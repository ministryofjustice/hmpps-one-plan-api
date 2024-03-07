package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.AuditAction
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.AuditTestBase

class StepAuditTests : AuditTestBase() {
  @BeforeEach
  fun purge() = purgeAuditQueue()

  @Test
  fun `Audit is sent on create step`() {
    val objectiveKey = givenAnObjective()
    val reference = givenAStep(objectiveKey)

    assertAuditMessageSent(AuditAction.CREATE_STEP, reference)
  }

  @Test
  fun `Audit is sent on delete step`() {
    val objectiveKey = givenAnObjective()
    val stepReference = givenAStep(objectiveKey)

    authedWebTestClient.delete()
      .uri(
        "/person/{crn}/objectives/{oRef}/steps/{sRef}",
        objectiveKey.caseReferenceNumber,
        objectiveKey.objectiveReference,
        stepReference,
      ).exchange()
      .expectStatus()
      .isNoContent()

    assertAuditMessageSent(AuditAction.DELETE_STEP, stepReference)
  }

  @Test
  fun `Audit is sent on update step`() {
    val objectiveKey = givenAnObjective()
    val stepReference = givenAStep(objectiveKey)

    authedWebTestClient.put()
      .uri(
        "/person/{crn}/objectives/{oRef}/steps/{sRef}",
        objectiveKey.caseReferenceNumber,
        objectiveKey.objectiveReference,
        stepReference,
      ).bodyValue(
        PutStepRequest(
          description = "newDesc",
          status = StepStatus.NOT_STARTED,
          reasonForChange = "testing",
          staffTask = true,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk()

    assertAuditMessageSent(AuditAction.UPDATE_STEP, stepReference)
  }

  @Test
  fun `Audit is sent on partial update step`() {
    val objectiveKey = givenAnObjective()
    val stepReference = givenAStep(objectiveKey)

    authedWebTestClient.patch()
      .uri(
        "/person/{crn}/objectives/{oRef}/steps/{sRef}",
        objectiveKey.caseReferenceNumber,
        objectiveKey.objectiveReference,
        stepReference,
      ).bodyValue(
        PatchStepRequest(
          description = "newDesc",
          reasonForChange = "testing",
        ),
      )
      .exchange()
      .expectStatus()
      .isOk()

    assertAuditMessageSent(AuditAction.UPDATE_STEP, stepReference)
  }
}
