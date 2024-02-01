package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import java.util.UUID

data class StepRequest(
  val description: String,
  val stepOrder: Int,
  val status: String,
) {
  fun buildEntity(objectiveId: UUID): StepEntity = StepEntity(
    objectiveId = objectiveId,
    description = description,
    status = status,
    stepOrder = stepOrder,
  )
}
