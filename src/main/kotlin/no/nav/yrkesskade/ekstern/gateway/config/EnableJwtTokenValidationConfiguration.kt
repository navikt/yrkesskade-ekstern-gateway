package no.nav.yrkesskade.ekstern.gateway.config

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.lang.invoke.MethodHandles
import java.net.URL

@Configuration
@EnableConfigurationProperties(MultiIssuerProperties::class)
class EnableJwtTokenValidationConfiguration(private val env: Environment) {
    val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    @Bean
    fun oidcResourceRetriever() = ProxyAwareResourceRetriever(
        configuredProxy(),
        env.getProperty("https.plaintext", Boolean::class.java, false)
    )

    @Bean
    fun multiIssuerConfiguration(
        issuerProperties: MultiIssuerProperties,
        resourceRetriever: ProxyAwareResourceRetriever?
    ) = MultiIssuerConfiguration(issuerProperties.issuer, resourceRetriever)

    private fun configuredProxy() = env.getProperty(
        env.getProperty("http.proxy.parametername", "http.proxy"), URL::class.java
    ).apply {
        if (env.getProperty("nais.cluster.name","local").contains("gcp")) {
            log.warn("You have enabled proxying in GCP, this is probably not what you want")
        }
    }
}