package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import java.util.UUID

class PlanControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var databaseClient: DatabaseClient

  @Test
  fun `Creates a plan on POST`() {
    authedWebTestClient.post().uri("/person/123/plans").bodyValue(CreatePlanRequest(PlanType.PERSONAL_LEARNING))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(CreateEntityResponse::class.java)
      .value { response -> assertThat(response.reference).isNotNull() }
  }

  @Test
  fun `Can GET a Plan`() {
    val (_, planReference) = givenAPlan("123")

    getPlan(crn = "123", planReference)
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.type").isEqualTo("PERSONAL_LEARNING")
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.reference").isEqualTo(planReference.toString())
      .jsonPath("$.createdBy").isEqualTo("test-user")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("test-user")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  @Test
  fun `Gives 404 when Plan does not exist`() {
    getPlan("123", UUID.randomUUID())
      .expectStatus().isNotFound()
  }

  private fun getPlan(crn: String, planReference: UUID): WebTestClient.ResponseSpec =
    authedWebTestClient.get()
      .uri("person/{crn}/plans/{reference}", crn, planReference)
      .exchange()

  @Test
  fun `Gives Empty list when Person does not exist`() {
    getAllExpectingCount("no-person", 0)
  }

  @Test
  fun `Can GET all plans for a person`() {
    val crn = "get-all"
    givenAPlan(crn, PlanType.PERSONAL_LEARNING)
    givenAPlan(crn, PlanType.SENTENCE)
    givenAPlan(crn, PlanType.RESETTLEMENT)

    getAllExpectingCount(crn, 3)
  }

  @Test
  fun `Can create a plan with a link to an existing objectives`() {
    val (caseReferenceNumber, objectiveReference1) = givenAnObjective(crn = "abc")
    val (_, objectiveReference2) = givenAnObjective(crn = "abc")

    val (planReference) = authedWebTestClient.post()
      .uri("/person/{crn}/plans", caseReferenceNumber)
      .bodyValue(
        CreatePlanRequest(
          planType = PlanType.PERSONAL_LEARNING,
          objectives = listOf(objectiveReference1, objectiveReference2),
        ),
      )
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(CreateEntityResponse::class.java)
      .returnResult()
      .responseBody!!

    runBlocking {
      assertThat(countLinkedObjectives(planReference))
        .describedAs("Should be 2 objectives linked to plan")
        .isEqualTo(2L)
    }
  }

  private suspend fun countLinkedObjectives(planReference: UUID): Long? {
    val sql = """ select count(*) as count from plan_objective_link where plan_id =
          | (select id from plan where reference = :planRef)
    """.trimMargin()
    return databaseClient.sql(sql)
      .bind("planRef", planReference)
      .fetch().one().map { it["count"] as Long }
      .awaitSingle()
  }

  @Test
  fun `404 On create plan if a given objective does not exist`() {
    val (caseReferenceNumber, objectiveReference1) = givenAnObjective(crn = "abc")

    val notFoundId = UUID.randomUUID()
    authedWebTestClient.post()
      .uri("/person/{crn}/plans", caseReferenceNumber)
      .bodyValue(
        CreatePlanRequest(
          planType = PlanType.PERSONAL_LEARNING,
          objectives = listOf(objectiveReference1, notFoundId),
        ),
      )
      .exchange()
      .expectStatus()
      .isNotFound()
      .expectBody()
      .jsonPath("$.userMessage")
      .value<String> { assertThat(it).contains(notFoundId.toString()) }
  }

  @Test
  fun `PATCH is not allowed`() {
    authedWebTestClient.patch()
      .uri("person/{crn}/plans", "456")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
  }

  @Test
  fun `PUT is not allowed`() {
    authedWebTestClient.put()
      .uri("person/{crn}/plans", "456")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
  }

  @Test
  fun `Can DELETE a plan, making it no long visible`() {
    val crn = "delete"
    val (_, planReference) = givenAPlan(crn, PlanType.PERSONAL_LEARNING)

    authedWebTestClient.delete()
      .uri("person/{crn}/plans/{plan}", crn, planReference.toString())
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
      .expectBody().isEmpty()

    val isDeletedInDb =
      databaseClient.sql(""" select is_deleted from plan where reference = :reference and crn = :crn """)
        .bind("reference", planReference)
        .bind("crn", crn)
        .fetch().one().map { it["is_deleted"] as Boolean }.block()
    assertThat(isDeletedInDb!!).describedAs("Db is_deleted flag should be true").isTrue()

    getAllExpectingCount(crn, 0)
    getPlan(crn, planReference)
      .expectStatus().isNotFound()
  }

  private fun getAllExpectingCount(crn: String, count: Int) {
    authedWebTestClient.get()
      .uri("person/{crn}/plans", crn)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isArray()
      .jsonPath("$.size()").isEqualTo(count)
  }
}
