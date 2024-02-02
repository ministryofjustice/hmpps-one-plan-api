package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import java.util.UUID

class StepControllerTest : IntegrationTestBase() {
  @Autowired
  private lateinit var databaseClient: DatabaseClient

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

    getStep(objectiveKey, stepRef)
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.isDeleted").doesNotExist()
      .jsonPath("$.description").isEqualTo("description")
      .jsonPath("$.status").isEqualTo("status")
      .jsonPath("$.stepOrder").isEqualTo(1)
      .jsonPath("$.reference").isEqualTo(stepRef.toString())
      .jsonPath("$.createdBy").isEqualTo("TODO")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("TODO")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  private fun getStep(
    objectiveKey: ObjectiveKey,
    stepRef: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.get()
    .uri(
      "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps/{stepRef}",
      objectiveKey.prisonNumber,
      objectiveKey.planReference,
      objectiveKey.objectiveReference,
      stepRef,
    )
    .exchange()

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

    getAllSteps(objectiveKey)
      .expectBody()
      .jsonPath("$.[*].reference")
      .value { refs: List<String> ->
        assertThat(refs)
          .containsExactlyInAnyOrder(stepReferenceA.toString(), stepReferenceB.toString())
      }
  }

  @Test
  fun `GET All Steps gives empty array when none are created`() {
    val objectiveKey = givenAnObjective()

    getAllSteps(objectiveKey)
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)
  }

  private fun getAllSteps(objectiveKey: ObjectiveKey): WebTestClient.ResponseSpec =
    authedWebTestClient.get()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps",
        objectiveKey.prisonNumber,
        objectiveKey.planReference,
        objectiveKey.objectiveReference,
      ).exchange()
      .expectStatus().isOk()

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

  @Test
  fun `DELETE Step marks step as is_deleted`() {
    val objectiveKey = givenAnObjective()
    val stepReference = givenAStep(objectiveKey)

    deleteStep(objectiveKey, stepReference)

    runBlocking {
      val isDeleted = databaseClient.sql("select is_deleted from step where reference = :ref")
        .bind("ref", stepReference)
        .fetch()
        .one()
        .map { it["is_deleted"] as Boolean }
        .awaitSingle()

      assertThat(isDeleted).describedAs("is_deleted should be true").isTrue()
    }
  }

  private fun deleteStep(
    objectiveKey: ObjectiveKey,
    stepReference: UUID,
  ) {
    authedWebTestClient.delete()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps/{stepRef}",
        objectiveKey.prisonNumber,
        objectiveKey.planReference,
        objectiveKey.objectiveReference,
        stepReference,
      ).exchange()
      .expectStatus()
      .isNoContent()
  }

  @Test
  fun `GET calls do not show a step after it is deleted`() {
    val objectiveKey = givenAnObjective()
    val stepReference = givenAStep(objectiveKey)
    deleteStep(objectiveKey, stepReference)

    getStep(objectiveKey, stepReference)
      .expectStatus()
      .isNotFound()

    getAllSteps(objectiveKey)
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)
  }

  @Test
  fun `DELETE 404 when Step does not exist`() {
    authedWebTestClient.delete()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps/{stepRef}",
        "123",
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
      ).exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `PUT updates a Step`() {
    val objective = givenAnObjective()
    val step = givenAStep(objective)

    authedWebTestClient.put()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{obj}/steps/{step}",
        objective.prisonNumber,
        objective.planReference,
        objective.objectiveReference,
        step,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
          {
            "description":"description2",
            "stepOrder": 2,
            "status": "status2"
          }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.description").isEqualTo("description2")
      .jsonPath("$.stepOrder").isEqualTo(2)
      .jsonPath("$.status").isEqualTo("status2")
  }

  @Test
  fun `404 on PUT if Step does not exist`() {
    authedWebTestClient.put()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{obj}/steps/{step}",
        "123",
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
          {
            "description":"description2",
            "stepOrder": 2,
            "status": "status2"
          }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `401 If not authed`() {
    webTestClient.delete()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{oReference}/steps/{stepRef}",
        "123",
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
      ).exchange()
      .expectStatus()
      .isUnauthorized()
  }
}
