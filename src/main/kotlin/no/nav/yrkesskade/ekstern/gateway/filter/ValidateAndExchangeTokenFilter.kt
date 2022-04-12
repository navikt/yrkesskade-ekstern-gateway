package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.yrkesskade.ekstern.gateway.tokenx.TokenXClient
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.invoke.MethodHandles

/**
 * kul beskrivelse
 */
@Component
class ValidateAndExchangeTokenFilter(
    val config: MultiIssuerConfiguration,
    val tokenXClient: TokenXClient
) : GlobalFilter {
    val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    val validationHandler = JwtTokenValidationHandler(config)

    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void> {
        val response = exchange!!.response
        val validatedTokens = validationHandler.getValidatedTokens(exchange.request)

        if (validatedTokens.hasValidToken()) {
            logger.info(validatedTokens.getJwtToken("maskinporten").tokenAsString)

            val exchangedToken = tokenXClient.exchange(
                validatedTokens.getJwtToken("maskinporten").tokenAsString,
                "yrkesskade-melding-api"
            )

            exchange.request.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $exchangedToken")
                .build()
        } else {
            return response.apply {
                this.setStatusCode(HttpStatus.UNAUTHORIZED)
            }.setComplete()
        }
        return chain!!.filter(exchange)
    }
}