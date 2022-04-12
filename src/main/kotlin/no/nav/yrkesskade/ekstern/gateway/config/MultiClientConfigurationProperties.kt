package no.nav.yrkesskade.ekstern.gateway.config

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.util.Objects
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

// tror ikke denne trengs.
//@Validated
//@ConfigurationProperties("no.nav.security.jwt.client")
//class ClientConfigurationProperties(val registration: @Valid Map<String, ClientProperties>)