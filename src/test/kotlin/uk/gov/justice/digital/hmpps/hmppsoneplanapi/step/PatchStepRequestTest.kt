package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class PatchStepRequestTest {

  private val entity = StepEntity(
    stepOrder = 1,
    staffTask = false,
    staffNote = "note",
    description = "desc",
    status = StepStatus.NOT_STARTED,
    objectiveId = UUID.randomUUID(),
    createdAtPrison = "prison1",
  )

  @Test
  fun `updates status`() {
    val result = PatchStepRequest(reasonForChange = "raison", status = StepStatus.IN_PROGRESS)
      .updateStepEntity(entity)

    assertThat(result.status).isEqualTo(StepStatus.IN_PROGRESS)
    assertNotChangedOtherThan("status", result)
  }

  @Test
  fun `updates note`() {
    val result = PatchStepRequest(reasonForChange = "raison", staffNote = "more noted")
      .updateStepEntity(entity)

    assertThat(result.staffNote).isEqualTo("more noted")
    assertNotChangedOtherThan("staffNote", result)
  }

  @Test
  fun `updates task`() {
    val result = PatchStepRequest(reasonForChange = "raison", staffTask = true)
      .updateStepEntity(entity)

    assertThat(result.staffTask).isTrue()
    assertNotChangedOtherThan("staffTask", result)
  }

  @Test
  fun `with createdAtPrison`() {
    val result = PatchStepRequest(reasonForChange = "raison", staffTask = true, updatedAtPrison = "prison2")
      .updateStepEntity(entity)

    assertThat(result.staffTask).isTrue()
    assertThat(result.createdAtPrison).isEqualTo("prison1")
    assertThat(result.updatedAtPrison).isEqualTo("prison2")
    assertNotChangedOtherThan("staffTask", result = result)
  }

  private fun assertNotChangedOtherThan(field: String, result: StepEntity) {
    assertThat(result)
      .usingRecursiveComparison()
      .ignoringFields(field, "isNew", "updatedAtPrison")
      .isEqualTo(entity)

    assertThat(result.isNew).isFalse()
  }
}
