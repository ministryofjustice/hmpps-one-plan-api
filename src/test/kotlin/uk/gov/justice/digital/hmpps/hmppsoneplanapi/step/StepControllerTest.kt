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
                "status": "IN_PROGRESS",
                "staffTask": false,
                "staffNote": "staff note"
        }
  """.trimIndent()

  @Test
  fun `Creates a step on POST`() {
    val (crn, objectiveReference) = givenAnObjective()

    authedWebTestClient.post()
      .uri(
        "/person/{crn}/objectives/{oReference}/steps",
        crn,
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
      .jsonPath("$.status").isEqualTo("IN_PROGRESS")
      .jsonPath("$.staffTask").isEqualTo(false)
      .jsonPath("$.staffNote").isEqualTo("staff note")
      .jsonPath("$.stepOrder").isEqualTo(1)
      .jsonPath("$.reference").isEqualTo(stepRef.toString())
      .jsonPath("$.createdBy").isEqualTo("test-user")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("test-user")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  private fun getStep(
    objectiveKey: ObjectiveKey,
    stepRef: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.get()
    .uri(
      "/person/{crn}/objectives/{oReference}/steps/{stepRef}",
      objectiveKey.caseReferenceNumber,
      objectiveKey.objectiveReference,
      stepRef,
    )
    .exchange()

  @Test
  fun `404 when step does not exist`() {
    authedWebTestClient.get()
      .uri(
        "/person/{crn}/objectives/{oReference}/steps/{stepRef}",
        "abc",
        UUID.randomUUID(),
        UUID.randomUUID(),
      ).exchange()
      .expectStatus()
      .isNotFound()
  }

  fun givenAStep(objectiveKey: ObjectiveKey, body: String = requestBody): UUID {
    val exchangeResult = authedWebTestClient.post()
      .uri(
        "/person/{crn}/objectives/{oReference}/steps",
        objectiveKey.caseReferenceNumber,
        objectiveKey.objectiveReference,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
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
      .expectBodyList(RefAndStepOrder::class.java)
      .contains(
        RefAndStepOrder(stepReferenceA.toString(), 1),
        RefAndStepOrder(stepReferenceB.toString(), 2),
      )
  }

  data class RefAndStepOrder(val reference: String, val stepOrder: Int)

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
        "/person/{crn}/objectives/{oReference}/steps",
        objectiveKey.caseReferenceNumber,
        objectiveKey.objectiveReference,
      ).exchange()
      .expectStatus().isOk()

  @Test
  fun `GET All Steps gives 404 when objective does not exist`() {
    authedWebTestClient.get()
      .uri(
        "/person/{crn}/objectives/{oReference}/steps",
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
      val (isDeleted, status) = databaseClient.sql("select is_deleted, status from step where reference = :ref")
        .bind("ref", stepReference)
        .fetch()
        .one()
        .map { it["is_deleted"] as Boolean to it["status"] as String }
        .awaitSingle()

      assertThat(isDeleted).describedAs("is_deleted should be true").isTrue()
      assertThat(status).isEqualTo(StepStatus.ARCHIVED.name)
    }
  }

  private fun deleteStep(
    objectiveKey: ObjectiveKey,
    stepReference: UUID,
  ) {
    authedWebTestClient.delete()
      .uri(
        "/person/{crn}/objectives/{oReference}/steps/{stepRef}",
        objectiveKey.caseReferenceNumber,
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
        "/person/{crn}/objectives/{oReference}/steps/{stepRef}",
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

    val body = """
                    {
                      "description":"description2",
                      "status": "COMPLETED",
                      "reasonForChange": "reason for change",
                      "staffTask": true
                    }
    """.trimIndent()
    putStep(objective, step, body)
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.description").isEqualTo("description2")
      .jsonPath("$.stepOrder").isEqualTo(1)
      .jsonPath("$.status").isEqualTo("COMPLETED")
      .jsonPath("$.staffTask").isEqualTo(true)

    val reasonForChangeOnHistoryRecord =
      databaseClient.sql(
        """ select reason_for_change from step_history where step_id =
        | (select step_id from step where reference = :reference)
        """.trimMargin(),
      )
        .bind("reference", step)
        .fetch().one().map { it["reason_for_change"] as String }.block()

    assertThat(reasonForChangeOnHistoryRecord).isEqualTo("reason for change")
  }

  private fun putStep(
    objective: ObjectiveKey,
    step: UUID,
    body: String,
  ): WebTestClient.ResponseSpec = authedWebTestClient.put()
    .uri(
      "/person/{crn}/objectives/{obj}/steps/{step}",
      objective.caseReferenceNumber,
      objective.objectiveReference,
      step,
    )
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(body)
    .exchange()

  @Test
  fun `404 on PUT if Step does not exist`() {
    authedWebTestClient.put()
      .uri(
        "/person/{crn}/objectives/{obj}/steps/{step}",
        "123",
        UUID.randomUUID(),
        UUID.randomUUID(),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
          {
            "description":"description2",
            "status": "COMPLETED",
            "reasonForChange": "a reason",
            "staffTask": false
          }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `400 When try to update a COMPLETED step`() {
    val requestBody = """
        {
                "description":"a completed step",
                "status": "COMPLETED",
                "staffTask": true,
                "reasonForChange": "This is actually unreasonable"
        }
    """.trimIndent()
    val objectiveKey = givenAnObjective()
    val stepRef = givenAStep(objectiveKey, requestBody)

    putStep(objectiveKey, stepRef, requestBody)
      .expectStatus()
      .isBadRequest()
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("cannot update completed Step")
  }
}
