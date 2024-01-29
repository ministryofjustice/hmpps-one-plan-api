package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
    val planReference = createPlan()

    webTestClient.get()
      .uri("person/{prisonNumber}/plans/{reference}", "123", planReference)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.type").isEqualTo("PERSONAL_LEARNING")
      .jsonPath("$.id").doesNotExist()
      .jsonPath("$.reference").isEqualTo(planReference.toString())
      .jsonPath("$.createdBy").isEqualTo("TODO")
      .jsonPath("$.createdAt").isNotEmpty
      .jsonPath("$.updatedBy").isEqualTo("TODO")
      .jsonPath("$.updatedAt").isNotEmpty
  }

  private fun createPlan(): UUID {
    return webTestClient.post().uri("/person/123/plans").bodyValue(CreatePlanRequest(PlanType.PERSONAL_LEARNING))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(CreatePlanResponse::class.java)
      .returnResult()
      .responseBody!!
      .reference
  }
}
