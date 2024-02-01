package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase

class StepControllerTest : IntegrationTestBase() {
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
      .jsonPath("$.reference").value { ref: String -> Assertions.assertThat(ref).hasSize(36) }
  }
}
