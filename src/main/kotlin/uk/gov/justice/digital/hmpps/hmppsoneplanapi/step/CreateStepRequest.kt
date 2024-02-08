package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateStepRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val description: String,
  val stepOrder: Int,
  @field:NotBlank
  @field:Size(min = 1, max = 50)
  val status: String,
) {
  fun buildEntity(objectiveId: UUID): StepEntity = StepEntity(
    objectiveId = objectiveId,
    description = description,
    status = status,
    stepOrder = stepOrder,
  )
}
