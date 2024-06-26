package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.sanitise
import java.time.LocalDate

data class PutObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val title: String,
  val type: ObjectiveType,
  val targetCompletionDate: LocalDate? = null,
  val status: ObjectiveStatus,
  val note: String? = null,
  val outcome: String? = null,
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,
  @field:Size(min = 0, max = 250)
  val updatedAtPrison: String? = null,
) : ObjectiveUpdate {
  override fun updateObjectiveEntity(original: ObjectiveEntity): ObjectiveEntity = original.copy(
    title = title.sanitise(),
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note?.sanitise(),
    outcome = outcome?.sanitise(),
    updatedAtPrison = updatedAtPrison?.sanitise(),
    type = type,
  ).markAsUpdate()
}

data class PatchObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 250)
  override val reasonForChange: String,

  @field:Size(min = 1, max = 512)
  val title: String? = null,
  val type: ObjectiveType? = null,
  val targetCompletionDate: LocalDate? = null,
  val status: ObjectiveStatus? = null,
  val note: String? = null,
  val outcome: String? = null,
  @field:Size(min = 0, max = 250)
  val updatedAtPrison: String? = null,
) : ObjectiveUpdate {
  override fun updateObjectiveEntity(original: ObjectiveEntity): ObjectiveEntity = original.copy(
    title = title?.sanitise() ?: original.title,
    targetCompletionDate = targetCompletionDate ?: original.targetCompletionDate,
    status = status ?: original.status,
    note = note?.sanitise() ?: original.note,
    outcome = outcome?.sanitise() ?: original.outcome,
    updatedAtPrison = updatedAtPrison?.sanitise(),
    type = type ?: original.type,
  ).markAsUpdate()
}

interface ObjectiveUpdate {
  val reasonForChange: String
  fun updateObjectiveEntity(original: ObjectiveEntity): ObjectiveEntity
}
