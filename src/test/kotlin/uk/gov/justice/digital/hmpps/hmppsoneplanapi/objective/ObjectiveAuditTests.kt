package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.AuditAction
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.AuditTestBase

class ObjectiveAuditTests : AuditTestBase() {
  @BeforeEach
  fun purge() = purgeAuditQueue()

  @Test
  fun `Audit is sent on create objective`() {
    val (_, reference) = givenAnObjective()

    assertAuditMessageSent(AuditAction.CREATE_OBJECTIVE, reference)
  }

  @Test
  fun `Audit is sent on delete objective`() {
    val (crn, reference) = givenAnObjective()

    authedWebTestClient.delete()
      .uri("/person/{crn}/objectives/{ref}", crn, reference)
      .exchange()
      .expectStatus()
      .isNoContent()

    assertAuditMessageSent(AuditAction.DELETE_OBJECTIVE, reference)
  }

  @Test
  fun `Audit is sent on update objective`() {
    val (crn, reference) = givenAnObjective()

    authedWebTestClient.put()
      .uri("/person/{crn}/objectives/{ref}", crn, reference)
      .bodyValue(
        PutObjectiveRequest(
          title = "title",
          reasonForChange = "just testing",
          status = ObjectiveStatus.NOT_STARTED,
          type = ObjectiveType.PERSONAL,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk()

    assertAuditMessageSent(AuditAction.UPDATE_OBJECTIVE, reference)
  }

  @Test
  fun `Audit is sent on partial update objective`() {
    val (crn, reference) = givenAnObjective()

    authedWebTestClient.patch()
      .uri("/person/{crn}/objectives/{ref}", crn, reference)
      .bodyValue(
        PatchObjectiveRequest(
          title = "title",
          reasonForChange = "just testing",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk()

    assertAuditMessageSent(AuditAction.UPDATE_OBJECTIVE, reference)
  }
}
