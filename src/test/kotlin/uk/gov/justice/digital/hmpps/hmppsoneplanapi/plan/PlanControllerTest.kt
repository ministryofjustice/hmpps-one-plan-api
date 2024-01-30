package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import java.util.UUID

class PlanControllerTest : IntegrationTestBase() {

  @Test
  fun `Creates a plan on POST`() {
    webTestClient.post().uri("/person/123/plans").bodyValue(CreatePlanRequest(PlanType.PERSONAL_LEARNING))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(CreatePlanResponse::class.java)
      .value { response -> assertThat(response.reference).isNotNull() }
  }

  @Test
  fun `Can GET a Plan`() {
    val planReference = createPlan("123")

    webTestClient.get()
      .uri("person/{prisonNumber}/plans/{reference}", "123", planReference)
      .exchange()
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
    webTestClient.get()
      .uri("person/{prisonNumber}/plans/{reference}", "123", UUID.randomUUID())
      .exchange()
      .expectStatus().isNotFound()
  }

  @Test
  fun `Gives Empty list when Person does not exist`() {
    webTestClient.get()
      .uri("person/{prisonNumber}/plans", "no-person")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isArray()
      .jsonPath("$.size()").isEqualTo(0)
  }

  @Test
  fun `Can GET all plans for a person`() {
    val prisonNumber = "get-all"
    createPlan(prisonNumber, PlanType.PERSONAL_LEARNING)
    createPlan(prisonNumber, PlanType.SENTENCE)
    createPlan(prisonNumber, PlanType.RESETTLEMENT)

    webTestClient.get()
      .uri("person/{prisonNumber}/plans", prisonNumber)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isArray()
      .jsonPath("$.size()").isEqualTo(3)
  }

  private fun createPlan(prisonNumber: String, type: PlanType = PlanType.PERSONAL_LEARNING): UUID {
    return webTestClient.post().uri("/person/{prisonNumber}/plans", prisonNumber)
      .bodyValue(CreatePlanRequest(planType = type))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(CreatePlanResponse::class.java)
      .returnResult()
      .responseBody!!
      .reference
  }

  @Test
  fun `PATCH is not allowed`() {
    webTestClient.patch()
      .uri("person/{prisonNumber}/plans", "456")
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
  }
}
