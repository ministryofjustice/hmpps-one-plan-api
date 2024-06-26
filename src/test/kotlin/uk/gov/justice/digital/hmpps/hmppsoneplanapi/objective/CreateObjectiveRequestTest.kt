package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate

class CreateObjectiveRequestTest {
  @Test
  fun mapsToEntity() {
    val entity = CreateObjectiveRequest(
      title = "title",
      targetCompletionDate = LocalDate.of(2024, 2, 1),
      status = ObjectiveStatus.IN_PROGRESS,
      note = "note",
      outcome = "outcome",
      createdAtPrison = "prison1",
      type = ObjectiveType.PERSONAL,
    ).buildEntity(CaseReferenceNumber("crn"))

    assertThat(entity.title).isEqualTo("title")
    assertThat(entity.targetCompletionDate).isEqualTo("2024-02-01")
    assertThat(entity.status).isEqualTo(ObjectiveStatus.IN_PROGRESS)
    assertThat(entity.note).isEqualTo("note")
    assertThat(entity.outcome).isEqualTo("outcome")
    assertThat(entity.caseReferenceNumber).isEqualTo(CaseReferenceNumber("crn"))
    assertThat(entity.createdAtPrison).isEqualTo("prison1")
    assertThat(entity.updatedAtPrison).isEqualTo("prison1")
    assertThat(entity.type).isEqualTo(ObjectiveType.PERSONAL)
    assertThat(entity.isNew).isTrue()
  }
}
