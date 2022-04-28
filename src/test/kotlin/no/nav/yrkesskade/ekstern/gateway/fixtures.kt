package no.nav.yrkesskade.ekstern.gateway

import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange

private const val HTTP_LOCALHOST_8080 = "http://localhost:8080"

fun kodeverkExchange(): ServerWebExchange {
    val request = MockServerHttpRequest.get("$HTTP_LOCALHOST_8080/api/v1/kodeverk/typer")
    val exchange: ServerWebExchange = MockServerWebExchange.from(request).apply {
        this.attributes[ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR] = Route.async()
            .id("yrkesskade-kodeverk")
            .uri(HTTP_LOCALHOST_8080)
            .predicate { true }
            .build()
    }
    return exchange
}

fun skademeldingExchange(): ServerWebExchange {
    val request = MockServerHttpRequest.post("$HTTP_LOCALHOST_8080/api/v1/skademeldinger").build()
    return yrkesskadeMeldingApiExchange(request)
}

fun apidocExchange(): ServerWebExchange {
    val request = MockServerHttpRequest.get("$HTTP_LOCALHOST_8080/api/v3/api-doc").build()
    return yrkesskadeMeldingApiExchange(request)
}

fun swaggerExchange(): ServerWebExchange {
    val request = MockServerHttpRequest.get("$HTTP_LOCALHOST_8080/api/swagger-ui/index.html").build()
    return yrkesskadeMeldingApiExchange(request)
}

fun yrkesskadeMeldingApiExchange(request: MockServerHttpRequest): ServerWebExchange {
    return MockServerWebExchange.from(request).apply {
        this.attributes[ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR] = Route.async()
            .id("yrkesskade-melding-api")
            .uri(HTTP_LOCALHOST_8080)
            .predicate { true }
            .build()
    }
}