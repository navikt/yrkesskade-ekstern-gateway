package no.nav.yrkesskade.ekstern.gateway.filter

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.yrkesskade.ekstern.gateway.ScopeValidator
import no.nav.yrkesskade.ekstern.gateway.apidocExchange
import no.nav.yrkesskade.ekstern.gateway.kodeverkExchange
import no.nav.yrkesskade.ekstern.gateway.skademeldingExchange
import no.nav.yrkesskade.ekstern.gateway.swaggerExchange
import no.nav.yrkesskade.ekstern.gateway.tokenx.TokenXClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono

@ActiveProfiles("test")
@SpringBootTest
internal class ValidateAndExchangeTokenFilterMockTest {

    @MockkBean
    lateinit var tokenXClient: TokenXClient

    @MockkBean
    lateinit var validationHandler: JwtTokenValidationHandler

    @MockkBean
    lateinit var scopeValidator: ScopeValidator

    @MockkBean
    lateinit var tokenValidationContext: TokenValidationContext

    @Autowired
    lateinit var tokenFilter: ValidateAndExchangeTokenFilter

    val token = JwtToken("eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJ3aGF0ZXZlciIsIm5iZiI6MTY0OTc1MzIwNiwiaXNzIjoiaHR0cHM6XC9cL2Zha2VkaW5ncy5kZXYtZ2NwLm5haXMuaW9cL2Zha2UiLCJleHAiOjE2NTMzNTMyMDYsImlhdCI6MTY0OTc1MzIwNiwiY29uc3VtZXIiOiJ7XCJhdXRob3JpdHlcIjpcImlzbzY1MjMtYWN0b3JpZC11cGlzXCIsXCJJRFwiOlwiMDE5Mjo5OTE4MjU4MjdcIn0iLCJqdGkiOiIyMTM3NTA3OS1kMDM0LTQxZmItYmFhZS1kOTgyNTg1ODBhZTUifQ.MPAXMoP0aHmm8Obbbm6gBq27cQaGzuihXaJ8khavG0K3hLfnzC9bX66X6sdHJuRiO5n0mu7fqPokH5ve40deeFL_dgkbVm_RiWDlgGZfQUg6TsT0PlfM4lZGSaibZ9D_d90ZS9ss-6B6JYDFOtiFIH_Rsb6QVZIRd2GobwM7j595xTNx8OIJbcb7l7QQTeQ40s0HTWSAuVI4jtWG0LqA4zyUhXWfmNgWLFJUSDn6WHJoOaZc4OkSNlL09lsfKhXubJJIwLo7pGUWuRFjaVTWzdTT1ADMmSE7EqfjXzrmxq2z3k8k6_atBOLocZraj0NIiO8zUxOTAzgX1IHZaM_kBQ")
    val filterChain = GatewayFilterChain { Mono.empty() }

    @BeforeEach
    fun setUp() {
        every { tokenValidationContext.hasValidToken() } answers { true }
        every { tokenValidationContext.getJwtToken(any()) } answers { token }
        every { validationHandler.getValidatedTokens(any()) } answers { tokenValidationContext }
        every { scopeValidator.validateScope(any(), any()) } answers { true }
        every { tokenXClient.exchange(any(), any(), any()) } answers { token.tokenAsString }
    }

    @Test
    fun `filter on skademelding request should perform tokenX`() {
        tokenFilter.filter(skademeldingExchange(), filterChain)
        verify(exactly = 1) { tokenXClient.exchange(any(), any(), any()) }
    }

    @Test
    fun `filter on swagger request should perform tokenX`() {
        tokenFilter.filter(swaggerExchange(), filterChain)
        verify(exactly = 1) { tokenXClient.exchange(any(), any(), any()) }
    }

    @Test
    fun `filter on apidoc request should perform tokenX`() {
        tokenFilter.filter(apidocExchange(), filterChain)
        verify(exactly = 1) { tokenXClient.exchange(any(), any(), any()) }
    }

    @Test
    fun `filter on kodeverk request should not perform tokenX`() {
        tokenFilter.filter(kodeverkExchange(), filterChain)
        verify(exactly = 0) { tokenXClient.exchange(any(), any(), any()) }
    }
}