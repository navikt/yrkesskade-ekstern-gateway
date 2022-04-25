package no.nav.yrkesskade.ekstern.gateway

import no.nav.yrkesskade.ekstern.gateway.config.ScopeValidationConfiguration
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange

@Component
class ScopeValidator(val scopeValidationConfiguration: ScopeValidationConfiguration) {

    fun validateScope(exchange: ServerWebExchange, scopeFromToken: String?): Boolean {
        val scopesForRoute = scopeValidationConfiguration.scopes[resolveRouteId(exchange)]
        val matchingScope = scopesForRoute?.find { route ->
            AntPathMatcher().match(route.path, exchange.request.path.toString())
        }

        if (matchingScope?.name.isNullOrBlank()) {
            return false
        }
        if (matchingScope?.acceptsAnyScope() == true) {
            return true
        }
        return matchingScope?.name == scopeFromToken
    }
}