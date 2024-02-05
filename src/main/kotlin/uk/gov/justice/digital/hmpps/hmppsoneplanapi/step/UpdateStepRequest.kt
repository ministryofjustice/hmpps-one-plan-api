package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

data class UpdateStepRequest(
  val description: String,
  val stepOrder: Int,
  val status: String,
  val reasonForChange: String,
) {
  fun updateEntity(entity: StepEntity) = entity.copy(
    description = description,
    stepOrder = stepOrder,
    status = status,
  ).markAsUpdate()
}
