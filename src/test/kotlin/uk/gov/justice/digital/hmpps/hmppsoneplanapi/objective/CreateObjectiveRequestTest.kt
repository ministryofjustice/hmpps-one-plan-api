package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
    ).buildEntity()

    assertThat(entity.title).isEqualTo("title")
    assertThat(entity.targetCompletionDate).isEqualTo("2024-02-01")
    assertThat(entity.status).isEqualTo(ObjectiveStatus.IN_PROGRESS)
    assertThat(entity.note).isEqualTo("note")
    assertThat(entity.outcome).isEqualTo("outcome")
  }
}
