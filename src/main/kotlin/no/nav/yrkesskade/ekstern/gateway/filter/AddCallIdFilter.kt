package no.nav.yrkesskade.ekstern.gateway.filter

import org.slf4j.MDC
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * Filter som legger til Nav-CallId i log-konteksten, samt request header dersom den ikke foreligger fra før.
 * Dette tilrettelegger for sporing på tvers av applikasjoner.
 */
@Component
@Order(1)
class AddCallIdFilter : GlobalFilter {
    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void> {
        MDC.clear()
        val callIdFromRequest = exchange!!.request.headers[CORRELATION_ID_HEADER_NAME]?.get(0)

        if (callIdFromRequest == null) {
            val newCallId = UUID.randomUUID().toString()
            exchange.request.mutate()
                .header(CORRELATION_ID_HEADER_NAME, newCallId)
                .build()
            MDC.put(CORRELATION_ID_LOG_VAR_NAME, newCallId)
        } else {
            MDC.put(CORRELATION_ID_LOG_VAR_NAME, callIdFromRequest)
        }
        return chain!!.filter(exchange)
    }

    companion object {
        private const val CORRELATION_ID_HEADER_NAME = "Nav-CallId"
        const val CORRELATION_ID_LOG_VAR_NAME = "correlationId"
    }
}