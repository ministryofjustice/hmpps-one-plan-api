package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
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
      .uri("/person/{crn}/plans/{pReference}/objectives", crn, planReference)
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
      .uri("/person/{crn}/plans/{pReference}/objectives", crn, planReference)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(minimalRequestBody)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.reference").value { ref: String -> assertThat(ref).hasSize(36) }
  }

  @Test
  fun `404 on create objective if plan not found`() {
    authedWebTestClient.post()
      .uri("/person/{crn}/plans/{pReference}/objectives", "nobody", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `401 on create objective if not authenticated`() {
    webTestClient.post()
      .uri("/person/{crn}/plans/{pReference}/objectives", "nobody", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .exchange()
      .expectStatus()
      .isUnauthorized()
  }

  @Test
  fun `GET Single objective`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)

    getObjective(planKey, objectiveReference)
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
  }

  @Test
  fun `GET Minimal objective`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey = planKey, body = minimalRequestBody)

    getObjective(planKey, objectiveReference)
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
    planKey: PlanKey,
    objectiveReference: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.get()
    .uri(
      "/person/{crn}/plans/{pReference}/objectives/{objReference}",
      planKey.caseReferenceNumber,
      planKey.reference,
      objectiveReference,
    ).exchange()

  fun givenAnObjective(planKey: PlanKey, body: String = requestBody): UUID {
    val exchangeResult = authedWebTestClient.post()
      .uri("/person/{crn}/plans/{pReference}/objectives", planKey.caseReferenceNumber, planKey.reference)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus().isOk()
      .expectBody(CreateEntityResponse::class.java)
      .returnResult()

    return exchangeResult.responseBody!!.reference
  }

  @Test
  fun `404 when plan does not exist`() {
    getObjective(PlanKey("not-exist", UUID.randomUUID()), UUID.randomUUID())
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `404 when objective does not exist`() {
    val aPlan = givenAPlan()

    getObjective(aPlan, UUID.randomUUID())
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `404 when plan is deleted`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)
    givenPlanIsDeleted(planKey)

    getObjective(planKey, objectiveReference)
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `PUT updates the objective`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)

    authedWebTestClient.put()
      .uri(
        "/person/{crn}/plans/{pReference}/objectives/{obj}",
        planKey.caseReferenceNumber,
        planKey.reference,
        objectiveReference,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
                "title":"title2",
                "targetCompletionDate": "2024-02-02",
                "status": "COMPLETED",
                "note":"note2",
                "outcome":"outcome2",
                "reasonForChange": "reason for change"
        }
        """.trimIndent(),
      )
      .exchange()
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
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)
    val minimalUpdateBody = """
        {
                "title":"title",
                "status":"COMPLETED",
                "reasonForChange": "Just felt like it"
        }
    """.trimIndent()

    authedWebTestClient.put()
      .uri(
        "/person/{crn}/plans/{pReference}/objectives/{obj}",
        planKey.caseReferenceNumber,
        planKey.reference,
        objectiveReference,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(minimalUpdateBody)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.title").isEqualTo("title")
      .jsonPath("$.targetCompletionDate").isEmpty()
      .jsonPath("$.note").isEmpty()
      .jsonPath("$.outcome").isEmpty()
  }

  @Test
  fun `DELETE Objective marks as is_deleted`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)

    deleteObjective(planKey, objectiveReference)
      .expectStatus()
      .isNoContent()

    val isDeletedInDb =
      databaseClient.sql(""" select is_deleted from objective where reference = :reference """)
        .bind("reference", objectiveReference)
        .fetch().one().map { it["is_deleted"] as Boolean }.block()
    assertThat(isDeletedInDb!!).describedAs("Db is_deleted flag should be true").isTrue()
  }

  private fun deleteObjective(
    planKey: PlanKey,
    objectiveReference: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.delete()
    .uri(
      "/person/{crn}/plans/{pReference}/objectives/{obj}",
      planKey.caseReferenceNumber,
      planKey.reference,
      objectiveReference,
    ).exchange()

  @Test
  fun `404 if try to GET deleted objective`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)

    deleteObjective(planKey, objectiveReference)
      .expectStatus()
      .isNoContent()

    getObjective(planKey, objectiveReference)
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `GET all objectives for a plan`() {
    val planKey = givenAPlan()
    val objectiveReferenceA = givenAnObjective(planKey)
    val objectiveReferenceB = givenAnObjective(planKey)

    authedWebTestClient.get()
      .uri("/person/{crn}/plans/{pReference}/objectives", planKey.caseReferenceNumber, planKey.reference)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.[*].reference")
      .value { refs: List<String> ->
        assertThat(refs).containsExactlyInAnyOrder(objectiveReferenceA.toString(), objectiveReferenceB.toString())
      }
  }

  @Test
  fun `Empty array on GET all when a plan has no objectives`() {
    val planKey = givenAPlan()

    authedWebTestClient.get()
      .uri("/person/{crn}/plans/{pReference}/objectives", planKey.caseReferenceNumber, planKey.reference)
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
}
