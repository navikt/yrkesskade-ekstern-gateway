package no.nav.yrkesskade.ekstern.gateway.config

import no.nav.security.token.support.core.configuration.IssuerProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
@ConfigurationProperties("no.nav.security.jwt")
class MultiIssuerProperties(@Valid val issuer: Map<String, IssuerProperties>)
