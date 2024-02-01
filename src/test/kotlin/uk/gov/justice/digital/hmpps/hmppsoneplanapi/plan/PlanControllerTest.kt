package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

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

    getPlan(prisonNumber = "123", planReference)
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.type").isEqualTo("PERSONAL_LEARNING")
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.reference").isEqualTo(planReference.toString())
      .jsonPath("$.createdBy").isEqualTo("TODO")
      .jsonPath("$.createdAt").isNotEmpty()
      .jsonPath("$.updatedBy").isEqualTo("TODO")
      .jsonPath("$.updatedAt").isNotEmpty()
  }

  @Test
  fun `Gives 404 when Plan does not exist`() {
    getPlan("123", UUID.randomUUID())
      .expectStatus().isNotFound()
  }

  private fun getPlan(prisonNumber: String, planReference: UUID): WebTestClient.ResponseSpec =
    authedWebTestClient.get()
      .uri("person/{prisonNumber}/plans/{reference}", prisonNumber, planReference)
      .exchange()

  @Test
  fun `Gives Empty list when Person does not exist`() {
    getAllExpectingCount("no-person", 0)
  }

  @Test
  fun `Can GET all plans for a person`() {
    val prisonNumber = "get-all"
    givenAPlan(prisonNumber, PlanType.PERSONAL_LEARNING)
    givenAPlan(prisonNumber, PlanType.SENTENCE)
    givenAPlan(prisonNumber, PlanType.RESETTLEMENT)

    getAllExpectingCount(prisonNumber, 3)
  }

  @Test
  fun `PATCH is not allowed`() {
    authedWebTestClient.patch()
      .uri("person/{prisonNumber}/plans", "456")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
  }

  @Test
  fun `PUT is not allowed`() {
    authedWebTestClient.put()
      .uri("person/{prisonNumber}/plans", "456")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
  }

  @Test
  fun `Can DELETE a plan, making it no long visible`() {
    val prisonNumber = "delete"
    val (_, planReference) = givenAPlan(prisonNumber, PlanType.PERSONAL_LEARNING)

    authedWebTestClient.delete()
      .uri("person/{prisonNumber}/plans/{plan}", prisonNumber, planReference.toString())
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
      .expectBody().isEmpty()

    val isDeletedInDb =
      databaseClient.sql(""" select is_deleted from plan where reference = :reference and prison_number = :pnumber """)
        .bind("reference", planReference)
        .bind("pnumber", prisonNumber)
        .fetch().one().map { it["is_deleted"] as Boolean }.block()
    assertThat(isDeletedInDb!!).describedAs("Db is_deleted flag should be true").isTrue()

    getAllExpectingCount(prisonNumber, 0)
    getPlan(prisonNumber, planReference)
      .expectStatus().isNotFound()
  }

  private fun getAllExpectingCount(prisonNumber: String, count: Int) {
    authedWebTestClient.get()
      .uri("person/{prisonNumber}/plans", prisonNumber)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isArray()
      .jsonPath("$.size()").isEqualTo(count)
  }

  @Test
  fun `401 when not authenticated`() {
    webTestClient.get()
      .uri("person/123/plans")
      .exchange()
      .expectStatus()
      .isUnauthorized()
  }
}
