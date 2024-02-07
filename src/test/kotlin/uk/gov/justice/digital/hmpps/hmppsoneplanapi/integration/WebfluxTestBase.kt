package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@ActiveProfiles("test")
@Import(JwtAuthTestOverride::class)
abstract class WebfluxTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient
  protected lateinit var authedWebTestClient: WebTestClient

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var jwtAuthHelper: JwtAuthTestOverride

  @BeforeEach
  fun setupAuth() {
    if (!::authedWebTestClient.isInitialized) {
      authedWebTestClient = webTestClient
        .mutateWith { builder, _, _ ->
          builder.defaultHeader(
            HttpHeaders.AUTHORIZATION,
            jwtAuthHelper.createAuthHeader(),
          )
        }
    }
  }
}
