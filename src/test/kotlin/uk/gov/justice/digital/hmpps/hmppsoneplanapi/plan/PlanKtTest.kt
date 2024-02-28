package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.ZonedDateTime
import java.util.UUID

class PlanKtTest {
  @Test
  fun `builds plan from entity`() {
    val entity = PlanEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      caseReferenceNumber = CaseReferenceNumber("crn"),
      type = PlanType.SENTENCE,
      updatedAt = ZonedDateTime.now(),
      createdAt = ZonedDateTime.now().minusDays(1),
      updatedBy = "someone",
      createdBy = "someoneElse",
    )

    val result = buildPlan(entity)

    assertThat(result.reference).isEqualTo(entity.reference)
    assertThat(result.caseReferenceNumber).isEqualTo(entity.caseReferenceNumber)
    assertThat(result.type).isEqualTo(entity.type)
    assertThat(result.updatedAt).isEqualTo(entity.updatedAt)
    assertThat(result.updatedBy).isEqualTo(entity.updatedBy)
    assertThat(result.createdAt).isEqualTo(entity.createdAt)
    assertThat(result.createdBy).isEqualTo(entity.createdBy)
    assertThat(result.objectives).isNull()
  }
}
