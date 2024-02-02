package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import java.util.UUID

class StepControllerTest : IntegrationTestBase() {
  val requestBody = """
        {
                "description":"description",
                "stepOrder": 1,
                "status": "status"
        }
  """.trimIndent()

  @Test
  fun `Creates a step on POST`() {
    val (prisonNumber, planReference, objectiveReference) = givenAnObjective()

    authedWebTestClient.post()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps",
        prisonNumber,
        planReference,
        objectiveReference,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.reference").value { ref: String -> assertThat(ref).hasSize(36) }
  }

  @Test
  fun `GET Single step`() {
    val objectiveKey = givenAnObjective()
    val stepRef = givenAStep(objectiveKey)

    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps/{stepRef}",
        objectiveKey.prisonNumber,
        objectiveKey.planReference,
        objectiveKey.objectiveReference,
        stepRef,
      )
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.description").isEqualTo("description")
      .jsonPath("$.status").isEqualTo("status")
      .jsonPath("$.stepOrder").isEqualTo(1)
      .jsonPath("$.reference").isEqualTo(stepRef.toString())
      .jsonPath("$.createdBy").isEqualTo("TODO")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("TODO")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  @Test
  fun `404 when step does not exist`() {
    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps/{stepRef}",
        "abc",
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
      ).exchange()
      .expectStatus()
      .isNotFound()
  }

  fun givenAStep(objectiveKey: ObjectiveKey): UUID {
    val exchangeResult = authedWebTestClient.post()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps",
        objectiveKey.prisonNumber,
        objectiveKey.planReference,
        objectiveKey.objectiveReference,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk()
      .expectBody(CreateEntityResponse::class.java)
      .returnResult()

    return exchangeResult.responseBody!!.reference
  }

  @Test
  fun `GET All Steps for an objective`() {
    val objectiveKey = givenAnObjective()
    val stepReferenceA = givenAStep(objectiveKey)
    val stepReferenceB = givenAStep(objectiveKey)

    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps",
        objectiveKey.prisonNumber,
        objectiveKey.planReference,
        objectiveKey.objectiveReference,
      ).exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.[*].reference")
      .value {
          refs: List<String> ->
        assertThat(refs)
          .containsExactlyInAnyOrder(stepReferenceA.toString(), stepReferenceB.toString())
      }
  }

  @Test
  fun `GET All Steps gives empty array when none are created`() {
    val objectiveKey = givenAnObjective()

    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps",
        objectiveKey.prisonNumber,
        objectiveKey.planReference,
        objectiveKey.objectiveReference,
      ).exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)
  }

  @Test
  fun `GET All Steps gives 404 when objective does not exist`() {
    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps",
        "123",
        UUID.randomUUID(),
        UUID.randomUUID(),
      ).exchange()
      .expectStatus().isNotFound()
  }
}
