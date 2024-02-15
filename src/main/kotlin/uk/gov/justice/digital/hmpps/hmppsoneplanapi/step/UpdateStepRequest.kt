package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateStepRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val description: String,
  val status: StepStatus,
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  val reasonForChange: String,
  @field:Size(min = 0, max = 512)
  val staffNote: String?,
  val staffTask: Boolean,
) {
  fun updateEntity(entity: StepEntity) = entity.copy(
    description = description,
    status = status,
    staffNote = staffNote,
    staffTask = staffTask,
  ).markAsUpdate()
}
