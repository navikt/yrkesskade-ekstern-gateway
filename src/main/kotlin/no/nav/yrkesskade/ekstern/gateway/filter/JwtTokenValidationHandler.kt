package no.nav.yrkesskade.ekstern.gateway.filter

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.jwt.JwtToken
import java.util.stream.Collectors
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.core.validation.JwtTokenValidator
import no.nav.security.token.support.core.configuration.IssuerConfiguration
import no.nav.security.token.support.core.exceptions.IssuerConfigurationException
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import java.util.AbstractMap.SimpleImmutableEntry
import java.util.Optional
import java.util.function.Supplier

class JwtTokenValidationHandler(private val config: MultiIssuerConfiguration) {

    fun getValidatedTokens(request: ServerHttpRequest): TokenValidationContext {
        val tokensFromRequest: List<JwtToken> = JwtTokenRetriever.getTokensFromHeader(config, request)
        val validatedTokens: Map<String, JwtToken> = tokensFromRequest
            .map { jwtToken: JwtToken -> validate(jwtToken) }
            .filter { obj: Optional<Map.Entry<String, JwtToken>> -> obj.isPresent }
            .map { obj: Optional<Map.Entry<String, JwtToken>> -> obj.get() }
            .associateBy({ it.key }, { it.value })

        LOG.debug(
            "found {} tokens on request, number of validated tokens is {}",
            tokensFromRequest.size,
            validatedTokens.size
        )
        if (validatedTokens.isEmpty() && !tokensFromRequest.isEmpty()) {
            LOG.debug("Found {} unvalidated token(s) with issuer(s) {} on request, is this a configuration error?",
                tokensFromRequest.size,
                tokensFromRequest.stream().map { obj: JwtToken -> obj.issuer }.collect(Collectors.toList())
            )
        }
        return TokenValidationContext(validatedTokens)
    }

    private fun validate(jwtToken: JwtToken): Optional<Map.Entry<String, JwtToken>> {
        return try {
            LOG.debug("check if token with issuer={} is present in config", jwtToken.issuer)
            if (config.getIssuer(jwtToken.issuer).isPresent) {
                val issuerShortName = issuerConfiguration(jwtToken.issuer).name
                LOG.debug(
                    "found token from trusted issuer={} with shortName={} in request",
                    jwtToken.issuer,
                    issuerShortName
                )
                val start = System.currentTimeMillis()
                tokenValidator(jwtToken).assertValidToken(jwtToken.tokenAsString)
                val end = System.currentTimeMillis()
                LOG.debug("validated token from issuer[{}] in {} ms", jwtToken.issuer, end - start)
                return Optional.of(entry<String, JwtToken>(issuerShortName, jwtToken))
            }
            LOG.debug("token is from an unknown issuer={}, skipping validation.", jwtToken.issuer)
            Optional.empty()
        } catch (e: JwtTokenValidatorException) {
            LOG.info(
                "found invalid token for issuer [{}, expires at {}], exception message:{} ",
                jwtToken.issuer,
                e.expiryDate,
                e.message
            )
            Optional.empty()
        }
    }

    private fun tokenValidator(jwtToken: JwtToken): JwtTokenValidator {
        return issuerConfiguration(jwtToken.issuer).tokenValidator
    }

    private fun issuerConfiguration(issuer: String): IssuerConfiguration {
        return config.getIssuer(issuer)
            .orElseThrow(issuerConfigurationException("could not find IssuerConfiguration for issuer=$issuer"))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(JwtTokenValidationHandler::class.java)
        private fun issuerConfigurationException(message: String): Supplier<IssuerConfigurationException> {
            return Supplier { IssuerConfigurationException(message) }
        }

        private fun <T, U> entry(key: T, value: U): Map.Entry<T, U> {
            return SimpleImmutableEntry(key, value)
        }
    }
}