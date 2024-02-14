package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateStepRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val description: String,
  val status: StepStatus,
  @field:Size(min = 0, max = 512)
  val staffNote: String?,
  val staffTask: Boolean,
) {
  fun buildEntity(objectiveId: UUID, order: Int): StepEntity = StepEntity(
    objectiveId = objectiveId,
    description = description,
    status = status,
    stepOrder = order,
    staffNote = staffNote,
    staffTask = staffTask,
  )
}
