package no.nav.yrkesskade.ekstern.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("token.tokenx")
@ConstructorBinding
class TokenXClientListProperties(val clientList: List<String>)