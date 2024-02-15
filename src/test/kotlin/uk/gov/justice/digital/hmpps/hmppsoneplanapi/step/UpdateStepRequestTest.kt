package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UpdateStepRequestTest {
  @Test
  fun `updates step entity`() {
    val original = StepEntity(
      objectiveId = UUID.randomUUID(),
      description = "desc",
      stepOrder = 1,
      status = StepStatus.IN_PROGRESS,
      staffNote = "Notational",
      staffTask = true,
    )

    val updated = UpdateStepRequest(
      description = "desc2",
      status = StepStatus.COMPLETED,
      reasonForChange = "reason for change",
      staffNote = null,
      staffTask = false,
    ).updateEntity(original)

    assertThat(updated.objectiveId).isEqualTo(original.objectiveId)
    assertThat(updated.status).isEqualTo(StepStatus.COMPLETED)
    assertThat(updated.stepOrder).isEqualTo(1)
    assertThat(updated.description).isEqualTo("desc2")
    assertThat(updated.staffNote).isNull()
    assertThat(updated.staffTask).isFalse()
    assertThat(updated.isNew).describedAs("Should be flagged as an update").isFalse()
  }
}
