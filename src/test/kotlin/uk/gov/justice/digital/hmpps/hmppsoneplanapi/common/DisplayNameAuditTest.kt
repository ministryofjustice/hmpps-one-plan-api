package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveStatus
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.PatchObjectiveRequest

@AutoConfigureWebTestClient(timeout = "PT1H")
class DisplayNameAuditTest : IntegrationTestBase() {
  @Test
  fun `display name audit on an update`() {
    val (caseReferenceNumber, objectiveReference) = givenAnObjective()

    val jwt = jwtAuthHelper.createJwt(
      subject = "another-user",
      roles = listOf("ROLE_ONE_PLAN_EDIT"),
      displayName = "Another User",
    )

    notAuthedWebTestClient.patch()
      .uri("/person/{crn}/objectives/{ref}", caseReferenceNumber, objectiveReference)
      .header(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
      .bodyValue(PatchObjectiveRequest(reasonForChange = "testing", status = ObjectiveStatus.BLOCKED))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody()
      .jsonPath("$.createdByDisplayName").isEqualTo("Test User")
      .jsonPath("$.updatedByDisplayName").isEqualTo("Another User")
  }
}
