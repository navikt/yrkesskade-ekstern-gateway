package no.nav.yrkesskade.ekstern.gateway

import no.nav.yrkesskade.ekstern.gateway.config.ScopeValidationConfiguration
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange

@Component
class ScopeValidator(val scopeValidationConfiguration: ScopeValidationConfiguration) {

    /**
     * Sjekker at scopet som ligger på tokenet er gyldig gitt pathen som ligger på requestet.
     * [scopeValidationConfiguration] inneholder en liste over hvilke scopes som er gyldige per path.
     * Hvert innslag i denne scope-lista har en nøkkel som må stemme overens med route id.
     * F.eks.:
     * <code>
     *     ...
     *      routes:
     *          - id: yrkesskade-melding-api
     *      ...
     *      scopes:
     *          - name: yrkesskade-melding-api
     *   </code>
     *
     * @param exchange request-response interaksjonen vi befinner oss i
     * @param scopeFromToken scopet fra Maskinporten-tokenet fra requestet
     */
    fun validateScope(exchange: ServerWebExchange, scopeFromToken: String?): Boolean {
        val scopesForRoute = scopeValidationConfiguration.scopes[exchange.getRouteId()]

        // Finn scope-navnet som tilhører den konkrete pathen som ligger på requestet
        val matchingScope = scopesForRoute?.find { route ->
            AntPathMatcher().match(route.path, exchange.request.path.toString())
        }

        if (matchingScope?.name.isNullOrBlank()) {
            // ikke definert noe scope for den aktuelle pathen
            return false
        }
        if (matchingScope?.acceptsAnyScope() == true) {
            return true
        }
        return matchingScope?.name == scopeFromToken
    }
}