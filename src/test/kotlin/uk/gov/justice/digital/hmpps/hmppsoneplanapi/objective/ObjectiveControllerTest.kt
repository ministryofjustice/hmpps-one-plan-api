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

    getObjective(planKey, objectiveReference)
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

  private fun getObjective(
    planKey: PlanKey,
    objectiveReference: UUID,
  ): WebTestClient.ResponseSpec = authedWebTestClient.get()
    .uri(
      "/person/{pNumber}/plans/{pReference}/objectives/{objReference}",
      planKey.prisonNumber,
      planKey.reference,
      objectiveReference,
    ).exchange()

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
  fun `PUT plan updates`() {
    val planKey = givenAPlan()
    val objectiveReference = givenAnObjective(planKey)

    authedWebTestClient.put()
      .uri(
        "/person/{pNumber}/plans/{pReference}/objectives/{obj}",
        planKey.prisonNumber,
        planKey.reference,
        objectiveReference,
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
                "title":"title2",
                "targetCompletionDate": "2024-02-02",
                "status":"status2",
                "note":"note2",
                "outcome":"outcome2"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.title").isEqualTo("title2")
      .jsonPath("$.targetCompletionDate").isEqualTo("2024-02-02")
      .jsonPath("$.status").isEqualTo("status2")
      .jsonPath("$.note").isEqualTo("note2")
      .jsonPath("$.outcome").isEqualTo("outcome2")
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
      "/person/{pNumber}/plans/{pReference}/objectives/{obj}",
      planKey.prisonNumber,
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
}
