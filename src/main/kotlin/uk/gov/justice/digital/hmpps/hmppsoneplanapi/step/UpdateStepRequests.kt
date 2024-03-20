package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.sanitise

data class PutStepRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val description: String,
  val status: StepStatus,
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,
  @field:Size(min = 0, max = 512)
  val staffNote: String? = null,
  val staffTask: Boolean,
  @field:Size(min = 0, max = 250)
  val updatedAtPrison: String? = null,
) : StepUpdate {
  override fun updateStepEntity(entity: StepEntity) = entity.copy(
    description = description.sanitise(),
    status = status,
    staffNote = staffNote?.sanitise(),
    staffTask = staffTask,
    updatedAtPrison = updatedAtPrison?.sanitise(),
  ).markAsUpdate()
}

data class PatchStepRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,
  val status: StepStatus? = null,
  @field:Size(min = 1, max = 512)
  val description: String? = null,
  @field:Size(min = 0, max = 512)
  val staffNote: String? = null,
  val staffTask: Boolean? = null,
  @field:Size(min = 0, max = 250)
  val updatedAtPrison: String? = null,
) : StepUpdate {
  override fun updateStepEntity(entity: StepEntity) = entity.copy(
    description = description?.sanitise() ?: entity.description,
    status = status ?: entity.status,
    staffNote = staffNote?.sanitise() ?: entity.staffNote,
    staffTask = staffTask ?: entity.staffTask,
    updatedAtPrison = updatedAtPrison?.sanitise(),
  ).markAsUpdate()
}

interface StepUpdate {
  val reasonForChange: String
  fun updateStepEntity(entity: StepEntity): StepEntity
}
