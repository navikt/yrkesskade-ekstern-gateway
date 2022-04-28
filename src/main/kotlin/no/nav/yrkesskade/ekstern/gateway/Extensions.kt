package no.nav.yrkesskade.ekstern.gateway

import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.web.server.ServerWebExchange

fun ServerWebExchange.getRouteId(): String {
    val route: Route = this.attributes[ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR] as Route
    return route.id
}