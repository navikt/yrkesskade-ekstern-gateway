package no.nav.yrkesskade.ekstern.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Klasse som inneholder en liste over hvilke ruter som krever TokenX.
 * Disse TokenX-klientnavnene må matche route id.
 * Eks:
 * <code>
 *     ...
 *      routes:
 *          - id: yrkesskade-melding-api
 *      ...
 *      client-list:
 *          - yrkesskade-melding-api
 *   </code>
 */
@ConfigurationProperties("token.tokenx")
@ConstructorBinding
class TokenXClientListProperties(val clientList: List<String>)