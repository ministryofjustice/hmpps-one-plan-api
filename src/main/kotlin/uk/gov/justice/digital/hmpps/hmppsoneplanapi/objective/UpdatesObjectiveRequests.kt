package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.sanitise
import java.time.LocalDate

data class PutObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val title: String,
  val targetCompletionDate: LocalDate? = null,
  val status: ObjectiveStatus,
  val note: String? = null,
  val outcome: String? = null,
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,
) : ObjectiveUpdate {
  override fun updateObjectiveEntity(original: ObjectiveEntity): ObjectiveEntity = original.copy(
    title = title.sanitise(),
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note?.sanitise(),
    outcome = outcome?.sanitise(),
  ).markAsUpdate()
}

data class PatchObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,

  @field:Size(min = 1, max = 512)
  val title: String? = null,
  val targetCompletionDate: LocalDate? = null,
  val status: ObjectiveStatus? = null,
  val note: String? = null,
  val outcome: String? = null,
) : ObjectiveUpdate {
  override fun updateObjectiveEntity(original: ObjectiveEntity): ObjectiveEntity = original.copy(
    title = title?.sanitise() ?: original.title,
    targetCompletionDate = targetCompletionDate ?: original.targetCompletionDate,
    status = status ?: original.status,
    note = note?.sanitise() ?: original.note,
    outcome = outcome?.sanitise() ?: original.outcome,
  ).markAsUpdate()
}

interface ObjectiveUpdate {
  val reasonForChange: String
  fun updateObjectiveEntity(original: ObjectiveEntity): ObjectiveEntity
}
