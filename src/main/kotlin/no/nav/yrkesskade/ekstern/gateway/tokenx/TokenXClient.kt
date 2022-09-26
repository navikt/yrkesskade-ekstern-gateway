package no.nav.yrkesskade.ekstern.gateway.tokenx

import no.nav.security.token.support.client.core.auth.ClientAssertion
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import java.lang.invoke.MethodHandles


@Component
@EnableConfigurationProperties(ClientConfigurationProperties::class)
class TokenXClient(
    val clientConfigurationProperties: ClientConfigurationProperties,
) {
    val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    /**
     * NB: navn på registration ([client]) må være identisk med gateway route-id
     * Eks:
     * <code>
     *     ...
     *      routes:
     *          - id: yrkesskade-melding-api
     *      ...
     *      client:
     *          registration:
     *              yrkesskade-melding-api:
     *   </code>
     */
    fun exchange(token: String, client: String, exchange: ServerWebExchange) {
        val clientProperties = clientConfigurationProperties.registration[client]
        if (clientProperties == null) {
            logger.error("Ingen clientProperties på client $client")
            throw RuntimeException("Ingen clientProperties på client $client")
        }

        val webClient = WebClient.builder()
            .baseUrl(clientProperties.tokenEndpointUrl.toString())
            .build()

        val clientAssertion = ClientAssertion(
            clientProperties.tokenEndpointUrl,
            clientProperties.authentication
        )

        val body: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", clientProperties.grantType.value)
            add("client_assertion_type", clientAssertion.assertionType())
            add("client_assertion", clientAssertion.assertion())
            add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
            add("subject_token", token)
            add("aud", clientProperties.tokenExchange.audience)
        }

        webClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(body))
                    .exchangeToMono {
                        exchange.request.mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer ${it.bodyToMono<String>().toFuture().get()}")
                            .build()
                         it.bodyToMono<String>()
                    }
    }
}