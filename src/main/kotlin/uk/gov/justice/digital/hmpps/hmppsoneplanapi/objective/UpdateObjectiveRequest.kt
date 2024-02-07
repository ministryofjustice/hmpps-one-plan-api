package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UpdateObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val title: String,
  val targetCompletionDate: LocalDate,
  @field:NotBlank
  @field:Size(min = 1, max = 50)
  val status: String,
  val note: String,
  val outcome: String,
  val reasonForChange: String,
) {
  fun updateEntity(objectiveEntity: ObjectiveEntity): ObjectiveEntity = objectiveEntity.copy(
    title = title,
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note,
    outcome = outcome,
  ).markAsUpdate()
}
