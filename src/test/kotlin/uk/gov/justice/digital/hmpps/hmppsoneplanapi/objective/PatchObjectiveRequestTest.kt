package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate

class PatchObjectiveRequestTest {

  private val entity = ObjectiveEntity(
    caseReferenceNumber = CaseReferenceNumber("1"),
    note = "note",
    title = "desc",
    type = ObjectiveType.FINANCE_AND_ID,
    status = ObjectiveStatus.NOT_STARTED,
    outcome = "outcome",
    targetCompletionDate = LocalDate.of(2024, 3, 1),
  )

  @Test
  fun `updates status`() {
    val result = PatchObjectiveRequest(reasonForChange = "raison", status = ObjectiveStatus.IN_PROGRESS)
      .updateObjectiveEntity(entity)

    assertThat(result.status).isEqualTo(ObjectiveStatus.IN_PROGRESS)
    assertNotChangedOtherThan("status", result)
  }

  @Test
  fun `updates title`() {
    val result = PatchObjectiveRequest(reasonForChange = "raison", title = "new title")
      .updateObjectiveEntity(entity)

    assertThat(result.title).isEqualTo("new title")
    assertNotChangedOtherThan("title", result)
  }

  @Test
  fun `updates outcome`() {
    val result = PatchObjectiveRequest(reasonForChange = "raison", outcome = "new outcome")
      .updateObjectiveEntity(entity)

    assertThat(result.outcome).isEqualTo("new outcome")
    assertNotChangedOtherThan("outcome", result)
  }

  @Test
  fun `updates target date`() {
    val newDate = LocalDate.of(2024, 1, 4)
    val result = PatchObjectiveRequest(reasonForChange = "raison", targetCompletionDate = newDate)
      .updateObjectiveEntity(entity)

    assertThat(result.targetCompletionDate).isEqualTo(newDate)
    assertNotChangedOtherThan("targetCompletionDate", result)
  }

  @Test
  fun `updates note`() {
    val result = PatchObjectiveRequest(reasonForChange = "raison", note = "new note")
      .updateObjectiveEntity(entity)

    assertThat(result.note).isEqualTo("new note")
    assertNotChangedOtherThan("note", result)
  }

  @Test
  fun `updates type`() {
    val result = PatchObjectiveRequest(reasonForChange = "raison", type = ObjectiveType.HEALTH)
      .updateObjectiveEntity(entity)

    assertThat(result.type).isEqualTo(ObjectiveType.HEALTH)
    assertNotChangedOtherThan("type", result)
  }

  private fun assertNotChangedOtherThan(field: String, result: ObjectiveEntity) {
    assertThat(result)
      .usingRecursiveComparison()
      .ignoringFields(field, "isNew")
      .isEqualTo(entity)

    assertThat(result.isNew).isFalse()
  }
}
