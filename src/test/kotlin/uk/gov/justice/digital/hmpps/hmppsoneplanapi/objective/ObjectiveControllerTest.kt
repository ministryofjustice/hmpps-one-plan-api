package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.step.StepEntity
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.step.StepStatus
import java.util.UUID

class ObjectiveControllerTest : IntegrationTestBase() {
  @Autowired
  private lateinit var databaseClient: DatabaseClient

  private val requestBody = """
        {
                "title":"title",
                "targetCompletionDate": "2024-02-01",
                "status":"IN_PROGRESS",
                "note":"note",
                "outcome":"outcome"
        }
  """.trimIndent()

  private val minimalRequestBody = """
        {
                "title":"title",
                "status":"IN_PROGRESS"
        }
  """.trimIndent()

  @Test
  fun `Creates an objective on POST`() {
    val (crn, planReference) = givenAPlan()

    authedWebTestClient.post()
      .uri("/person/{crn}/objectives", crn, planReference)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.reference").value { ref: String -> assertThat(ref).hasSize(36) }
  }

  @Test
  fun `Creates minimal objective on POST`() {
    val (crn, planReference) = givenAPlan()

    authedWebTestClient.post()
      .uri("/person/{crn}/objectives", crn, planReference)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(minimalRequestBody)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.reference").value { ref: String -> assertThat(ref).hasSize(36) }
  }

  @Test
  fun `404 on create objective if given plan not found`() {
    authedWebTestClient.post()
      .uri("/person/{crn}/objectives", "123")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(createObjectiveAndLinkToPlan(UUID.randomUUID()))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `GET Single objective`() {
    val objectiveReference = givenAnObjective("123")

    getObjective(CaseReferenceNumber("123"), objectiveReference)
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.title").isEqualTo("title")
      .jsonPath("$.targetCompletionDate").isEqualTo("2024-02-01")
      .jsonPath("$.status").isEqualTo("IN_PROGRESS")
      .jsonPath("$.note").isEqualTo("note")
      .jsonPath("$.outcome").isEqualTo("outcome")
      .jsonPath("$.reference").isEqualTo(objectiveReference.toString())
      .jsonPath("$.createdBy").isEqualTo("test-user")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("test-user")
      .jsonPath("$.updatedAt").isNotEmpty()
      .jsonPath("$.steps").doesNotExist()
  }

  @Test
  fun `GET Minimal objective`() {
    val objectiveReference = givenAnObjective(crn = "456", body = minimalRequestBody)

    getObjective(CaseReferenceNumber("456"), objectiveReference)
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.title").isEqualTo("title")
      .jsonPath("$.targetCompletionDate").isEmpty()
      .jsonPath("$.status").isEqualTo("IN_PROGRESS")
      .jsonPath("$.note").isEmpty()
      .jsonPath("$.outcome").isEmpty()
      .jsonPath("$.reference").isEqualTo(objectiveReference.toString())
      .jsonPath("$.createdBy").isEqualTo("test-user")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("test-user")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  private fun getObjective(
    crn: CaseReferenceNumber,
    objectiveReference: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.get()
    .uri(
      "/person/{crn}/objectives/{objReference}",
      crn,
      objectiveReference,
    ).exchange()

  fun givenAnObjective(crn: String, body: String = requestBody): UUID {
    val exchangeResult = authedWebTestClient.post()
      .uri("/person/{crn}/objectives", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus().isOk()
      .expectBody(CreateEntityResponse::class.java)
      .returnResult()

    return exchangeResult.responseBody!!.reference
  }

  @Test
  fun `404 when objective does not exist`() {
    getObjective(CaseReferenceNumber("123"), UUID.randomUUID())
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `PUT updates the objective`() {
    val (crn, objectiveReference) = givenAnObjective()

    val requestBody = """
        {
                "title":"title2",
                "targetCompletionDate": "2024-02-02",
                "status": "COMPLETED",
                "note":"note2",
                "outcome":"outcome2",
                "reasonForChange": "reason for change"
        }
    """.trimIndent()

    putObjective(crn, objectiveReference, requestBody)
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.title").isEqualTo("title2")
      .jsonPath("$.targetCompletionDate").isEqualTo("2024-02-02")
      .jsonPath("$.status").isEqualTo("COMPLETED")
      .jsonPath("$.note").isEqualTo("note2")
      .jsonPath("$.outcome").isEqualTo("outcome2")

    val reasonForChangeOnHistoryRecord =
      databaseClient.sql(
        """ select reason_for_change from objective_history where objective_id =
        | (select id from objective where reference = :reference)
        """.trimMargin(),
      )
        .bind("reference", objectiveReference)
        .fetch().one().map { it["reason_for_change"] as String }.block()

    assertThat(reasonForChangeOnHistoryRecord).isEqualTo("reason for change")
  }

  @Test
  fun `PUT with minimal update body`() {
    val (crn, objectiveReference) = givenAnObjective()
    val minimalUpdateBody = """
        {
                "title":"title",
                "status":"COMPLETED",
                "reasonForChange": "Just felt like it"
        }
    """.trimIndent()

    putObjective(crn, objectiveReference, minimalUpdateBody)
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.title").isEqualTo("title")
      .jsonPath("$.targetCompletionDate").isEmpty()
      .jsonPath("$.note").isEmpty()
      .jsonPath("$.outcome").isEmpty()
  }

  private fun putObjective(
    crn: CaseReferenceNumber,
    objectiveReference: UUID,
    requestBody: String,
  ): WebTestClient.ResponseSpec = authedWebTestClient.put()
    .uri(
      "/person/{crn}/objectives/{obj}",
      crn,
      objectiveReference,
    )
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(requestBody)
    .exchange()

  @Test
  fun `DELETE Objective marks as is_deleted`() {
    val (crn, objectiveReference) = givenAnObjective()

    deleteObjective(crn, objectiveReference)
      .expectStatus()
      .isNoContent()

    val (isDeletedInDb, status) =
      databaseClient.sql(""" select is_deleted,status from objective where reference = :reference """)
        .bind("reference", objectiveReference)
        .fetch().one().map { it["is_deleted"] as Boolean to it["status"] as String }.block()!!
    assertThat(isDeletedInDb).describedAs("Db is_deleted flag should be true").isTrue()
    assertThat(status).isEqualTo(StepStatus.ARCHIVED.name)
  }

  private fun deleteObjective(
    crn: CaseReferenceNumber,
    objectiveReference: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.delete()
    .uri(
      "/person/{crn}/objectives/{obj}",
      crn,
      objectiveReference,
    ).exchange()

  @Test
  fun `404 if try to GET deleted objective`() {
    val (crn, objectiveReference) = givenAnObjective()

    deleteObjective(crn, objectiveReference)
      .expectStatus()
      .isNoContent()

    getObjective(crn, objectiveReference)
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `GET all objectives for a plan`() {
    val planKey = givenAPlan()
    val objectiveReferenceA = givenAnObjective(crn = "123", body = createObjectiveAndLinkToPlan(planKey.reference))
    val objectiveReferenceB = givenAnObjective(crn = "123", body = createObjectiveAndLinkToPlan(planKey.reference))

    authedWebTestClient.get()
      .uri("/person/{crn}/plans/{pReference}/objectives", planKey.crn, planKey.reference)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.[*].reference")
      .value { refs: List<String> ->
        assertThat(refs).containsExactlyInAnyOrder(objectiveReferenceA.toString(), objectiveReferenceB.toString())
      }
  }

  private fun createObjectiveAndLinkToPlan(planReference: UUID): String = """
            {
                    "title":"title",
                    "targetCompletionDate": "2024-02-01",
                    "status":"IN_PROGRESS",
                    "note":"note",
                    "outcome":"outcome",
                    "planReference": "$planReference"
            }
  """.trimIndent()

  @Test
  fun `Empty array on GET all when a plan has no objectives`() {
    val planKey = givenAPlan()

    authedWebTestClient.get()
      .uri("/person/{crn}/plans/{pReference}/objectives", planKey.crn, planKey.reference)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)
  }

  @Test
  fun `404 on GET all when a plan does not exist`() {
    authedWebTestClient.get()
      .uri("/person/{crn}/plans/{pReference}/objectives", "pie", UUID.randomUUID())
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `400 When try to update a COMPLETED objective`() {
    val requestBody = """
        {
                "title":"Random change",
                "status":"IN_PROGRESS",
                "reasonForChange": "Just felt like it"
        }
    """.trimIndent()
    val (caseReferenceNumber, objectiveReference) = givenAnObjective(status = ObjectiveStatus.COMPLETED)

    putObjective(caseReferenceNumber, objectiveReference, requestBody)
      .expectStatus()
      .isBadRequest()
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("cannot update completed Objective")
  }

  @Test
  fun `Get all objectives for a person`() {
    givenAnObjective(crn = "569")
    givenAnObjective(crn = "569")

    authedWebTestClient.get()
      .uri("/person/569/objectives")
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.size()").isEqualTo(2)
      .jsonPath("$.[0].title").isNotEmpty()
      .jsonPath("$.[0].targetCompletionDate").isNotEmpty()
      .jsonPath("$.[0].status").isNotEmpty()
      .jsonPath("$.[0].note").isNotEmpty()
      .jsonPath("$.[0].outcome").isNotEmpty()
      .jsonPath("$.[0].createdBy").isNotEmpty()
      .jsonPath("$.[0].createdAt").isNotEmpty()
      .jsonPath("$.[0].updatedAt").isNotEmpty()
      .jsonPath("$.[0].updatedBy").isNotEmpty()
  }

  @Test
  fun `Get all objectives for a person with steps`() {
    val objectiveWithNoStepsRef = givenAnObjective(crn = "899")
    val objectiveWithStepsRef = givenAnObjective(crn = "899")
    val key = ObjectiveKey(CaseReferenceNumber("899"), objectiveWithStepsRef)
    givenAStep(key)
    givenAStep(key)

    val response = authedWebTestClient.get()
      .uri("/person/899/objectives?includeSteps=true")
      .exchange()
      .expectStatus()
      .isOk()
      .expectBodyList(Objective::class.java)
      .hasSize(2)
      .returnResult().responseBody!!

    val objectiveWithSteps = response.find { it.reference == objectiveWithStepsRef }!!
    assertThat(objectiveWithSteps.steps).hasSize(2)
    assertThat(objectiveWithSteps.steps).extracting(StepEntity::staffNote, StepEntity::createdAt)
      .doesNotContainNull()
    val objectiveWithoutSteps = response.find { it.reference == objectiveWithNoStepsRef }!!
    assertThat(objectiveWithoutSteps.steps).hasSize(0)
  }
}
