package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate

class PutObjectiveRequestTest {
  @Test
  fun `updates entity`() {
    val original = ObjectiveEntity(
      title = "title",
      targetCompletionDate = LocalDate.of(2024, 2, 1),
      status = ObjectiveStatus.IN_PROGRESS,
      note = "note",
      outcome = "outcome",
      caseReferenceNumber = CaseReferenceNumber("crn"),
      createdAtPrison = "prison1",
    )

    val updated = PutObjectiveRequest(
      title = "title2",
      targetCompletionDate = LocalDate.of(2024, 2, 2),
      status = ObjectiveStatus.COMPLETED,
      note = "note2",
      outcome = "outcome2",
      reasonForChange = "reason for change",
      updatedAtPrison = "prison2",
    ).updateObjectiveEntity(original)

    assertThat(updated.title).isEqualTo("title2")
    assertThat(updated.targetCompletionDate).isEqualTo("2024-02-02")
    assertThat(updated.status).isEqualTo(ObjectiveStatus.COMPLETED)
    assertThat(updated.note).isEqualTo("note2")
    assertThat(updated.outcome).isEqualTo("outcome2")
    assertThat(updated.createdAtPrison).isEqualTo(original.createdAtPrison)
    assertThat(updated.isNew).describedAs("should be seen as update to db mapping").isFalse()
  }
}
