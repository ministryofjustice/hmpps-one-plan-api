package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.WebfluxTestBase
import java.time.LocalDate
import java.util.UUID

@WebFluxTest(controllers = [ObjectiveController::class])
class ObjectiveControllerValidationTests : WebfluxTestBase() {

  @MockkBean
  private lateinit var objectiveService: ObjectiveService

  @Test
  fun `400 when title is too long`() {
    val aLotOfAs = "a".repeat(1000)
    val body = createRequestBuilder(title = aLotOfAs)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("title: size must be between 1 and 512")
    }
  }

  @Test
  fun `400 when title is blank`() {
    val body = createRequestBuilder(title = "      \n")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("title: must not be blank")
    }
  }

  @Test
  fun `400 when title is null`() {
    val body = createRequestBuilder(title = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("title: is required")
    }
  }

  private fun post(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/123/plans/{ref}/objectives", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)

  private fun createRequestBuilder(
    title: String? = "title",
    targetCompletionDate: LocalDate? = LocalDate.of(2024, 2, 6),
    status: String? = "status",
    note: String? = "note",
    outcome: String? = "outcome",
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "title" to title,
          "targetCompletionDate" to targetCompletionDate,
          "status" to status,
          "note" to note,
          "outcome" to outcome,
        ).filter { it.value != null },
      )
  }
}
