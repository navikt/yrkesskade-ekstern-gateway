package no.nav.yrkesskade.ekstern.gateway.tokenx

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.assertj.core.api.Assertions

private const val TOKENX_CLIENT_NAME = "yrkesskade-melding-api"

@ActiveProfiles("test")
@SpringBootTest
internal class TokenXClientTest {

    @Autowired
    lateinit var tokenXClient: TokenXClient

    val token = "eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJ3aGF0ZXZlciIsIm5iZiI6MTY0OTc1MzIwNiwiaXNzIjoiaHR0cHM6XC9cL2Zha2VkaW5ncy5kZXYtZ2NwLm5haXMuaW9cL2Zha2UiLCJleHAiOjE2NTMzNTMyMDYsImlhdCI6MTY0OTc1MzIwNiwiY29uc3VtZXIiOiJ7XCJhdXRob3JpdHlcIjpcImlzbzY1MjMtYWN0b3JpZC11cGlzXCIsXCJJRFwiOlwiMDE5Mjo5OTE4MjU4MjdcIn0iLCJqdGkiOiIyMTM3NTA3OS1kMDM0LTQxZmItYmFhZS1kOTgyNTg1ODBhZTUifQ.MPAXMoP0aHmm8Obbbm6gBq27cQaGzuihXaJ8khavG0K3hLfnzC9bX66X6sdHJuRiO5n0mu7fqPokH5ve40deeFL_dgkbVm_RiWDlgGZfQUg6TsT0PlfM4lZGSaibZ9D_d90ZS9ss-6B6JYDFOtiFIH_Rsb6QVZIRd2GobwM7j595xTNx8OIJbcb7l7QQTeQ40s0HTWSAuVI4jtWG0LqA4zyUhXWfmNgWLFJUSDn6WHJoOaZc4OkSNlL09lsfKhXubJJIwLo7pGUWuRFjaVTWzdTT1ADMmSE7EqfjXzrmxq2z3k8k6_atBOLocZraj0NIiO8zUxOTAzgX1IHZaM_kBQ"

    @BeforeEach
    fun setUp() {

    }

    @Test
    fun exchange() {
        val exchangeToken = tokenXClient.exchange(token, TOKENX_CLIENT_NAME)
        Assertions.assertThat(exchangeToken).isNotNull
        println(exchangeToken)
    }

    @Test
    fun getClientConfigurationProperties() {
    }
}