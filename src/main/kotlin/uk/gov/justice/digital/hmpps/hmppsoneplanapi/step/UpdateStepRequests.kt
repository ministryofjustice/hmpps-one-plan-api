package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PutStepRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val description: String,
  val status: StepStatus,
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,
  @field:Size(min = 0, max = 512)
  val staffNote: String?,
  val staffTask: Boolean,
) : StepUpdate {
  override fun updateStepEntity(entity: StepEntity) = entity.copy(
    description = description,
    status = status,
    staffNote = staffNote,
    staffTask = staffTask,
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
) : StepUpdate {
  override fun updateStepEntity(entity: StepEntity) = entity.copy(
    description = description ?: entity.description,
    status = status ?: entity.status,
    staffNote = staffNote ?: entity.staffNote,
    staffTask = staffTask ?: entity.staffTask,
  ).markAsUpdate()
}

interface StepUpdate {
  val reasonForChange: String
  fun updateStepEntity(entity: StepEntity): StepEntity
}
