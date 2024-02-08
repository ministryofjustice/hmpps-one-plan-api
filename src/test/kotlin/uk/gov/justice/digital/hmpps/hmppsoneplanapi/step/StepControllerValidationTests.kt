package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.WebfluxTestBase
import java.util.UUID

@WebFluxTest(controllers = [StepController::class])
class StepControllerValidationTests : WebfluxTestBase() {

  @MockkBean
  private lateinit var stepService: StepService

  @Test
  fun `Post - 400 when description field is too long`() {
    val body = createRequestBuilder(description = "B".repeat(513))
    post(body).value {
      assertThat(it.userMessage).isEqualTo("description: size must be between 1 and 512")
    }
  }

  @Test
  fun `Post - 400 when description field is null`() {
    val body = createRequestBuilder(description = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("description: is required")
    }
  }

  @Test
  fun `Post - 400 when description field is blank`() {
    val body = createRequestBuilder(description = "\n   ")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("description: must not be blank")
    }
  }

  private fun createRequestBuilder(
    description: String? = "description",
    stepOrder: String? = "1",
    status: String? = "status",
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "description" to description,
          "status" to status,
          "stepOrder" to stepOrder,
        ).filter { it.value != null },
      )
  }

  private fun post(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/123/plans/{ref}/objectives/{oRef}/steps", UUID.randomUUID(), UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)
}
