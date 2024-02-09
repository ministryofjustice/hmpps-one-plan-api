package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.WebfluxTestBase

@WebFluxTest(controllers = [PlanController::class])
class PlanControllerValidationTests : WebfluxTestBase() {

  @MockkBean
  private lateinit var planService: PlanService

  @Test
  fun `400 on POST when crn is too long`() {
    post("12345678901", requestBuilder())
      .value { assertThat(it.userMessage).isEqualTo("crn: size must be between 1 and 10") }
  }

  @Test
  fun `400 on POST when crn is blank`() {
    post("  ", requestBuilder())
      .value { assertThat(it.userMessage).isEqualTo("crn: must not be blank") }
  }

  @Test
  fun `400 on POST when plan type is missing`() {
    post("crn123", requestBuilder(type = null))
      .value { assertThat(it.userMessage).isEqualTo("planType: is required") }
  }

  @Test
  fun `400 on POST when plan type is not one of the allowed values`() {
    post("crn123", requestBuilder(type = "ICE_CREAM"))
      .value { assertThat(it.userMessage).isEqualTo("planType: should be one of [PERSONAL_LEARNING, SENTENCE, RESETTLEMENT]") }
  }

  @Test
  fun `400 on GET when UUID is invalid`() {
    authedWebTestClient.get()
      .uri("/person/123/plans/not-a-uuid")
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)
      .value {
        assertThat(it.userMessage).isEqualTo("planReference: should be a valid UUID")
      }
  }

  private fun requestBuilder(type: Any? = "PERSONAL_LEARNING"): String =
    objectMapper.writeValueAsString(
      mapOf(
        "planType" to type,
      ),
    )

  private fun post(crn: String, body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/{crn}/plans", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)
}
