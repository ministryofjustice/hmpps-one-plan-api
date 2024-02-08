package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import jakarta.validation.constraints.Size

data class UpdateStepRequest(
  val description: String,
  val stepOrder: Int,
  val status: String,
  val reasonForChange: String,
  @field:Size(min = 0, max = 512)
  val staffNote: String?,
  val staffTask: Boolean,
) {
  fun updateEntity(entity: StepEntity) = entity.copy(
    description = description,
    stepOrder = stepOrder,
    status = status,
    staffNote = staffNote,
    staffTask = staffTask,
  ).markAsUpdate()
}
