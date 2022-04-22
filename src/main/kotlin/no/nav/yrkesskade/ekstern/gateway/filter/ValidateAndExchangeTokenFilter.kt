package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.yrkesskade.ekstern.gateway.config.ScopeValidationConfiguration
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
@EnableConfigurationProperties(ScopeValidationConfiguration::class)
class ValidateAndExchangeTokenFilter(
    val tokenXClient: TokenXClient,
    val validationHandler: JwtTokenValidationHandler,
    val scopeValidationConfiguration: ScopeValidationConfiguration
) : GlobalFilter {
    val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void> {
        val response = exchange!!.response
        val validatedTokens = validationHandler.getValidatedTokens(exchange.request)

        if (validatedTokens.hasValidToken()) {
            val maskinportenToken = validatedTokens.getJwtToken(MASKINPORTEN)
            logger.info(maskinportenToken.tokenAsString)

            val validScope = validateScope(exchange, maskinportenToken.jwtTokenClaims.get("scope") as String)
            if (!validScope) {
                return respondWithError(response, HttpStatus.FORBIDDEN)
            }

            val exchangedToken = tokenXClient.exchange(maskinportenToken.tokenAsString, resolveJwtClient(exchange))

            exchange.request.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer $exchangedToken")
                .header("x-nav-ys-kilde", "ekstern")
                .build()
        } else {
            return respondWithError(response, HttpStatus.UNAUTHORIZED)
        }
        return chain!!.filter(exchange)
    }

    private fun respondWithError(response: ServerHttpResponse, status: HttpStatus): Mono<Void> {
        return response.apply {
            this.setStatusCode(status)
        }.setComplete()
    }

    private fun validateScope(exchange: ServerWebExchange, scopeFromToken: String): Boolean {
        val scopesForRoute = scopeValidationConfiguration.scopes[resolveJwtClient(exchange)]
        val matchingScope = scopesForRoute?.find { route ->
            AntPathMatcher().match(route.path, exchange.request.path.toString())
        }

        if (matchingScope?.name.isNullOrBlank()) {
            return false
        }
        if (matchingScope?.acceptsAnyScope() == true) {
            return true
        }
        return matchingScope?.name == scopeFromToken
    }

    private fun resolveJwtClient(exchange: ServerWebExchange): String {
        val route: Route = exchange.attributes[ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR] as Route
        return route.id
    }
}