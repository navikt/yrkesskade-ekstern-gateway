package no.nav.yrkesskade.ekstern.gateway.tokenx

import no.nav.security.token.support.client.core.auth.ClientAssertion
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CompletableFuture


@Component
@EnableConfigurationProperties(ClientConfigurationProperties::class)
class TokenXClient(
    val clientConfigurationProperties: ClientConfigurationProperties,
) {

    fun exchange(token: String, client: String): String? {
        val clientProperties = clientConfigurationProperties.registration[client]
        if (clientProperties == null) {
            throw RuntimeException("ingen clientProperties på client $client")
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

        val response: CompletableFuture<String?> = CompletableFuture<String?>()
        webClient.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono<String>()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe { resp -> response.complete(resp) }

        return response.get()
    }
}