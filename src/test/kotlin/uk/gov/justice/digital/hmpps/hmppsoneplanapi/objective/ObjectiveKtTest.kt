package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class ObjectiveKtTest {
  @Test
  fun `builds and objective from an entity`() {
    val entity = ObjectiveEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      status = ObjectiveStatus.COMPLETED,
      caseReferenceNumber = CaseReferenceNumber("crn"),
      note = "note",
      targetCompletionDate = LocalDate.now(),
      outcome = "outcome",
      title = "title",
      updatedAt = ZonedDateTime.now(),
      createdAt = ZonedDateTime.now().minusDays(1),
      updatedBy = "someone",
      createdBy = "someoneElse",
    )

    val result = buildObjective(entity)
    assertThat(result.id).isEqualTo(entity.id)
    assertThat(result.reference).isEqualTo(entity.reference)
    assertThat(result.status).isEqualTo(entity.status)
    assertThat(result.caseReferenceNumber).isEqualTo(entity.caseReferenceNumber)
    assertThat(result.note).isEqualTo(entity.note)
    assertThat(result.targetCompletionDate).isEqualTo(entity.targetCompletionDate)
    assertThat(result.title).isEqualTo(entity.title)
    assertThat(result.outcome).isEqualTo(entity.outcome)
    assertThat(result.updatedAt).isEqualTo(entity.updatedAt)
    assertThat(result.updatedBy).isEqualTo(entity.updatedBy)
    assertThat(result.createdAt).isEqualTo(entity.createdAt)
    assertThat(result.createdBy).isEqualTo(entity.createdBy)
    assertThat(result.steps).isNull()
  }
}
