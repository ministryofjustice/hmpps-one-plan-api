package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ObjectiveRequestTest {
  @Test
  fun mapsToEntity() {
    val entity = ObjectiveRequest(
      title = "title",
      targetCompletionDate = LocalDate.of(2024, 2, 1),
      status = "status",
      note = "note",
      outcome = "outcome",
    ).buildEntity()

    assertThat(entity.title).isEqualTo("title")
    assertThat(entity.targetCompletionDate).isEqualTo("2024-02-01")
    assertThat(entity.status).isEqualTo("status")
    assertThat(entity.note).isEqualTo("note")
    assertThat(entity.outcome).isEqualTo("outcome")
  }

  @Test
  fun `updates entity`() {
    val original = ObjectiveEntity(
      title = "title",
      targetCompletionDate = LocalDate.of(2024, 2, 1),
      status = "status",
      note = "note",
      outcome = "outcome",
    )

    val updated = ObjectiveRequest(
      title = "title2",
      targetCompletionDate = LocalDate.of(2024, 2, 2),
      status = "status2",
      note = "note2",
      outcome = "outcome2",
    ).updateEntity(original)

    assertThat(updated.title).isEqualTo("title2")
    assertThat(updated.targetCompletionDate).isEqualTo("2024-02-02")
    assertThat(updated.status).isEqualTo("status2")
    assertThat(updated.note).isEqualTo("note2")
    assertThat(updated.outcome).isEqualTo("outcome2")
  }
}
