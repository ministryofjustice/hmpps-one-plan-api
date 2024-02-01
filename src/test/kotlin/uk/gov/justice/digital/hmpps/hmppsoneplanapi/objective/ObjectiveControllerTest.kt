package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import java.util.UUID

class ObjectiveControllerTest : IntegrationTestBase() {

  val planRequestBody = """
        {
                "title":"title",
                "targetCompletionDate": "2024-02-01",
                "status":"status",
                "note":"note",
                "outcome":"outcome"
        }
  """.trimIndent()

  @Test
  fun `Creates an objective on POST`() {
    val (prisonNumber, planReference) = givenAPlan()

    authedWebTestClient.post()
      .uri("/person/{pNumber}/plans/{pReference}/objectives", prisonNumber, planReference)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(planRequestBody)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.reference").value { ref: String -> assertThat(ref).hasSize(36) }
  }

  @Test
  fun `404 on create objective if plan not found`() {
    authedWebTestClient.post()
      .uri("/person/{pNumber}/plans/{pReference}/objectives", "nobody", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(planRequestBody)
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `401 on create objective if not authenticated`() {
    webTestClient.post()
      .uri("/person/{pNumber}/plans/{pReference}/objectives", "nobody", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(planRequestBody)
      .exchange()
      .expectStatus()
      .isUnauthorized()
  }
}
