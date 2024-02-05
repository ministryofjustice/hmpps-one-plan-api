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
      status = "status",
    )

    val updated = UpdateStepRequest(
      "desc2",
      2,
      "status2",
      "reason for change",
    ).updateEntity(original)

    assertThat(updated.objectiveId).isEqualTo(original.objectiveId)
    assertThat(updated.status).isEqualTo("status2")
    assertThat(updated.stepOrder).isEqualTo(2)
    assertThat(updated.description).isEqualTo("desc2")
    assertThat(updated.isNew).describedAs("Should be flagged as an update").isFalse()
  }
}
