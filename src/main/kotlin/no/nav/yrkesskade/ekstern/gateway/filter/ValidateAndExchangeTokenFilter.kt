package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.yrkesskade.ekstern.gateway.ScopeValidator
import no.nav.yrkesskade.ekstern.gateway.config.ScopeValidationConfiguration
import no.nav.yrkesskade.ekstern.gateway.config.TokenXClientListProperties
import no.nav.yrkesskade.ekstern.gateway.resolveRouteId
import no.nav.yrkesskade.ekstern.gateway.tokenx.TokenXClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.invoke.MethodHandles

private const val MASKINPORTEN = "maskinporten"

/**
 * Validerer Maskinporten-token og veksler i TokenX før requestet sendes videre til riktig uri.
 */
@Component
@EnableConfigurationProperties(value = [ScopeValidationConfiguration::class, TokenXClientListProperties::class])
class ValidateAndExchangeTokenFilter(
    val tokenXClient: TokenXClient,
    val validationHandler: JwtTokenValidationHandler,
    val scopeValidator: ScopeValidator,
    val tokenXClientListProperties: TokenXClientListProperties
) : GlobalFilter {

    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void> {
        val response = exchange!!.response
        val validatedTokens = validationHandler.getValidatedTokens(exchange.request)

        if (!validatedTokens.hasValidToken()) {
            return respondWithError(response, HttpStatus.UNAUTHORIZED)
        }

        val maskinportenToken = validatedTokens.getJwtToken(MASKINPORTEN)

        val validScope = scopeValidator.validateScope(exchange, maskinportenToken.jwtTokenClaims.get("scope") as String?)
        if (!validScope) {
            return respondWithError(response, HttpStatus.FORBIDDEN)
        }

        exchangeTokenIfNecessary(maskinportenToken, exchange)

        exchange.request.mutate()
            .header("x-nav-ys-kilde", "ekstern")
            .build()
        return chain!!.filter(exchange)
    }

    private fun exchangeTokenIfNecessary(maskinportenToken: JwtToken, exchange: ServerWebExchange) {
        val client = resolveRouteId(exchange)
        if (!tokenXClientListProperties.clientList.contains(client)) {
            return
        }

        val exchangedToken = tokenXClient.exchange(maskinportenToken.tokenAsString, client)
        exchange.request.mutate()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $exchangedToken")
            .build()
    }

    private fun respondWithError(response: ServerHttpResponse, status: HttpStatus): Mono<Void> {
        return response.apply {
            this.setStatusCode(status)
        }.setComplete()
    }
}