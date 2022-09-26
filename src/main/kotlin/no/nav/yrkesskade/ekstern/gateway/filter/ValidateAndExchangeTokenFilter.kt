package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.yrkesskade.ekstern.gateway.ScopeValidator
import no.nav.yrkesskade.ekstern.gateway.config.ScopeValidationConfiguration
import no.nav.yrkesskade.ekstern.gateway.config.TokenXClientListProperties
import no.nav.yrkesskade.ekstern.gateway.getRouteId
import no.nav.yrkesskade.ekstern.gateway.tokenx.TokenXClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.invoke.MethodHandles

private const val MASKINPORTEN = "maskinporten"

private const val YS_KILDE_HEADER_NAME = "x-nav-ys-kilde"

private const val YS_KILDE_EKSTERN = "ekstern"

/**
 * Validerer Maskinporten-token og veksler i TokenX før requestet sendes videre til riktig uri.
 */
@Component
@Order(2)
@EnableConfigurationProperties(value = [ScopeValidationConfiguration::class, TokenXClientListProperties::class])
class ValidateAndExchangeTokenFilter(
    val tokenXClient: TokenXClient,
    val validationHandler: JwtTokenValidationHandler,
    val scopeValidator: ScopeValidator,
    val tokenXClientListProperties: TokenXClientListProperties
) : GlobalFilter {
    val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void> {
        val response = exchange!!.response
        val validatedTokens = validationHandler.getValidatedTokens(exchange.request)

        if (!validatedTokens.hasValidToken()) {
            logger.info("Ingen gyldige tokens")
            return errorResponse(response, HttpStatus.UNAUTHORIZED)
        }

        val maskinportenToken = validatedTokens.getJwtToken(MASKINPORTEN)

        val scopeFromToken = maskinportenToken.jwtTokenClaims.get("scope") as String?
        val validScope = scopeValidator.validateScope(exchange, scopeFromToken)
        if (!validScope) {
            logger.info("Ugyldig scope $scopeFromToken for path ${exchange.request.path}")
            return errorResponse(response, HttpStatus.FORBIDDEN)
        }

        exchangeTokenIfNecessary(maskinportenToken, exchange)

        exchange.request.mutate()
            .header(YS_KILDE_HEADER_NAME, YS_KILDE_EKSTERN)
            .build()
        return chain!!.filter(exchange)
    }

    private fun exchangeTokenIfNecessary(maskinportenToken: JwtToken, exchange: ServerWebExchange) {
        val client = exchange.getRouteId()
        if (!tokenXClientListProperties.clientList.contains(client)) {
            logger.info("Ingen treff i TokenX-lista for klient $client. Utfører ikke tokenX.")
            return
        }

        logger.info("Fant treff i TokenX-lista for klient $client. Utfører tokenX.")
        tokenXClient.exchange(maskinportenToken.tokenAsString, client, exchange)
        logger.info("Utført tokenX")
    }

    private fun errorResponse(response: ServerHttpResponse, status: HttpStatus): Mono<Void> {
        return response.apply {
            this.statusCode = status
        }.setComplete()
    }
}