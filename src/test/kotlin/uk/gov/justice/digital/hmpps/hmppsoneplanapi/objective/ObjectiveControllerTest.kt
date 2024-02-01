package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
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

  @Test
  fun `GET Single objective`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)

    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{objReference}",
        planKey.prisonNumber,
        planKey.reference,
        objectiveReference,
      ).exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.title").isEqualTo("title")
      .jsonPath("$.targetCompletionDate").isEqualTo("2024-02-01")
      .jsonPath("$.status").isEqualTo("status")
      .jsonPath("$.note").isEqualTo("note")
      .jsonPath("$.outcome").isEqualTo("outcome")
      .jsonPath("$.reference").isEqualTo(objectiveReference.toString())
      .jsonPath("$.createdBy").isEqualTo("TODO")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("TODO")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  fun givenAnObjective(planKey: PlanKey): UUID {
    val exchangeResult = authedWebTestClient.post()
      .uri("/person/{pNumber}/plans/{pReference}/objectives", planKey.prisonNumber, planKey.reference)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(planRequestBody)
      .exchange()
      .expectStatus().isOk()
      .expectBody(CreateEntityResponse::class.java)
      .returnResult()

    return exchangeResult.responseBody!!.reference
  }
}
