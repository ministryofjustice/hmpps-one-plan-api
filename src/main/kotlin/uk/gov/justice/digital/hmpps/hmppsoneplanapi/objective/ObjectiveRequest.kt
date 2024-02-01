package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import java.time.LocalDate

data class ObjectiveRequest(
  val title: String,
  val targetCompletionDate: LocalDate,
  val status: String,
  val note: String,
  val outcome: String,
) {
  fun buildEntity(): ObjectiveEntity = ObjectiveEntity(
    title = title,
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note,
    outcome = outcome,
  )

  fun updateEntity(objectiveEntity: ObjectiveEntity): ObjectiveEntity = objectiveEntity.copy(
    title = title,
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note,
    outcome = outcome,
  )
}
