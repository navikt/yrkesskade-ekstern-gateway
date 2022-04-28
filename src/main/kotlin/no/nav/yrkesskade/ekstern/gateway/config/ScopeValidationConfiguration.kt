package no.nav.yrkesskade.ekstern.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * En klasse som inneholder alle scopes som er definert for hver path, innenfor hver route id.
 * Hvert innslag i denne scope-lista har en nøkkel som må stemme overens med route id.
 * F.eks.:
 * <code>
 *     ...
 *      routes:
 *          - id: yrkesskade-melding-api
 *      ...
 *      scopes:
 *          - name: yrkesskade-melding-api
 *   </code>
 *
 * Pathene følger Ant-style patterns.
 * Vi er nødt til å eksplisitt definere hvilke pather som ikke krever noe spesifikt scope. Dette er løst ved å gi disse
 * scopene en prefix ved navn "noscope".
 */
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