package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.AuditAction
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.AuditTestBase

class PlanAuditTests : AuditTestBase() {

  @BeforeEach
  fun purge() = purgeAuditQueue()

  @Test
  fun `Audit is sent on delete plan`() {
    val (crn, planReference) = givenAPlan()

    authedWebTestClient.delete()
      .uri("person/{crn}/plans/{plan}", crn, planReference)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)

    assertAuditMessageSent(auditAction = AuditAction.DELETE_PLAN, planReference)
  }

  @Test
  fun `Audit is sent on create plan`() {
    val (_, planReference) = givenAPlan()

    assertAuditMessageSent(auditAction = AuditAction.CREATE_PLAN, planReference)
  }
}
