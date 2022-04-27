package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.yrkesskade.ekstern.gateway.ScopeValidator
import no.nav.yrkesskade.ekstern.gateway.config.ScopeValidationConfiguration
import no.nav.yrkesskade.ekstern.gateway.config.TokenXClientListProperties
import no.nav.yrkesskade.ekstern.gateway.getRouteId
import no.nav.yrkesskade.ekstern.gateway.tokenx.TokenXClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val MASKINPORTEN = "maskinporten"

private const val YS_KILDE_HEADER_NAME = "x-nav-ys-kilde"

private const val YS_KILDE_HEADER_VALUE = "ekstern"

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
            return errorResponse(response, HttpStatus.UNAUTHORIZED)
        }

        val maskinportenToken = validatedTokens.getJwtToken(MASKINPORTEN)

        val validScope = scopeValidator.validateScope(exchange, maskinportenToken.jwtTokenClaims.get("scope") as String?)
        if (!validScope) {
            return errorResponse(response, HttpStatus.FORBIDDEN)
        }

        exchangeTokenIfNecessary(maskinportenToken, exchange)

        exchange.request.mutate()
            .header(YS_KILDE_HEADER_NAME, YS_KILDE_HEADER_VALUE)
            .build()
        return chain!!.filter(exchange)
    }

    private fun exchangeTokenIfNecessary(maskinportenToken: JwtToken, exchange: ServerWebExchange) {
        val client = exchange.getRouteId()
        if (!tokenXClientListProperties.clientList.contains(client)) {
            return
        }

        val exchangedToken = tokenXClient.exchange(maskinportenToken.tokenAsString, client)
        exchange.request.mutate()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $exchangedToken")
            .build()
    }

    private fun errorResponse(response: ServerHttpResponse, status: HttpStatus): Mono<Void> {
        return response.apply {
            this.statusCode = status
        }.setComplete()
    }
}