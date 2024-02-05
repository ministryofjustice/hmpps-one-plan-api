package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import java.time.LocalDate

data class UpdateObjectiveRequest(
  val title: String,
  val targetCompletionDate: LocalDate,
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
