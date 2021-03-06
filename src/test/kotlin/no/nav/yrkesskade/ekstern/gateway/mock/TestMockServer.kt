package no.nav.yrkesskade.ekstern.gateway.mock

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    value = arrayOf("service.wiremock.enabled"),
    havingValue = "true",
    matchIfMissing = false
)
class TestMockServer() : AbstractMockServer(null)
