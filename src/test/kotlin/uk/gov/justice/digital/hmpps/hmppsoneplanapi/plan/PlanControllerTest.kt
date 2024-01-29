package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase

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
}
