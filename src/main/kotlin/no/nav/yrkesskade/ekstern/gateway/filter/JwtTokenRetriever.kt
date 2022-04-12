package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.jwt.JwtToken
import java.util.stream.Collectors
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import java.lang.Exception

object JwtTokenRetriever {
    private val LOG = LoggerFactory.getLogger(JwtTokenRetriever::class.java)
    private const val BEARER = "Bearer"

    fun getTokensFromHeader(config: MultiIssuerConfiguration, request: ServerHttpRequest): List<JwtToken> {
        try {
            LOG.debug("checking authorization header for tokens")
            val authorization = request.headers[HttpHeaders.AUTHORIZATION]?.get(0)
            if (authorization != null) {
                val headerValues = authorization.split(",").toTypedArray()
                return extractBearerTokens(*headerValues)
                    .stream()
                    .map { encodedToken: String? -> JwtToken(encodedToken) }
                    .filter { jwtToken: JwtToken -> config.getIssuer(jwtToken.issuer).isPresent }
                    .collect(Collectors.toList())
            }
            LOG.debug("no tokens found in authorization header")
        } catch (e: Exception) {
            LOG.warn("received exception when attempting to extract and parse token from Authorization header", e)
        }
        return emptyList()
    }

    private fun extractBearerTokens(vararg headerValues: String): List<String> {
        return headerValues
            .map { s: String -> s.split(" ").toTypedArray() }
            .filter { pair: Array<String> -> pair.size == 2 }
            .filter { pair: Array<String> ->
                pair[0].trim { it <= ' ' }
                    .equals(BEARER, ignoreCase = true)
            }
            .map { pair: Array<String> -> pair[1].trim { it <= ' ' } }
    }
}