package no.nav.yrkesskade.ekstern.gateway

import no.nav.yrkesskade.ekstern.gateway.mock.TestMockServerInitialization
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.server.ServerWebExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(initializers = [TestMockServerInitialization::class])
internal class ScopeValidatorTest {

    @Autowired
    lateinit var scopeValidator: ScopeValidator

    @Test
    fun `validateScope - correct scope for kodeverk yields true`() {
        val result = scopeValidator.validateScope(kodeverkExchange(), "nav:yrkesskade:kodeverk.read")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - non-matching scope for kodeverk yields false`() {
        val result = scopeValidator.validateScope(kodeverkExchange(), "nav:yrkesskade:skademelding.write")
        Assertions.assertThat(result).isFalse
    }

    @Test
    fun `validateScope - nonexistent scope for kodeverk yields false`() {
        val result = scopeValidator.validateScope(kodeverkExchange(), null)
        Assertions.assertThat(result).isFalse
    }

    @Test
    fun `validateScope - correct scope for skademelding yields true`() {
        val result = scopeValidator.validateScope(skademeldingExchange(), "nav:yrkesskade:skademelding.write")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - incorrect scope for skademelding yields false`() {
        val result = scopeValidator.validateScope(skademeldingExchange(), "nav:yrkesskade:skattemelding.write")
        Assertions.assertThat(result).isFalse
    }

    @Test
    fun `validateScope - nonexistent scope for skademelding yields false`() {
        val result = scopeValidator.validateScope(skademeldingExchange(), null)
        Assertions.assertThat(result).isFalse
    }

    @Test
    fun `validateScope - any scope for apidoc yields true`() {
        val result = scopeValidator.validateScope(apidocExchange(), "any scope")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - kodeverk scope for apidoc yields true`() {
        val result = scopeValidator.validateScope(apidocExchange(), "nav:yrkesskade:kodeverk.read")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - nonexistent scope for apidoc yields true`() {
        val result = scopeValidator.validateScope(apidocExchange(), null)
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - empty scope for apidoc yields true`() {
        val result = scopeValidator.validateScope(apidocExchange(), "")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - any scope for swagger yields true`() {
        val result = scopeValidator.validateScope(swaggerExchange(), "any scope")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - kodeverk scope for swagger yields true`() {
        val result = scopeValidator.validateScope(swaggerExchange(), "nav:yrkesskade:kodeverk.read")
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - nonexistent scope for swagger yields true`() {
        val result = scopeValidator.validateScope(swaggerExchange(), null)
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `validateScope - empty scope for swagger yields true`() {
        val result = scopeValidator.validateScope(swaggerExchange(), "")
        Assertions.assertThat(result).isTrue
    }
}