package no.nav.yrkesskade.ekstern.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("token.validation")
@ConstructorBinding
class ScopeValidationConfiguration(val scopes: Map<String, List<ScopeValidationProperties>>)

private const val NOSCOPE = "noscope"

class ScopeValidationProperties(
    val name: String,
    val path: String
) {
    fun acceptsAnyScope() = name.startsWith(NOSCOPE)
}