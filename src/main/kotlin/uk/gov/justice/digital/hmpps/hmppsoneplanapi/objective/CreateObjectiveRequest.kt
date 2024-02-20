package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate

data class CreateObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val title: String,
  val targetCompletionDate: LocalDate?,
  val status: ObjectiveStatus,
  val note: String?,
  val outcome: String?,
) {
  fun buildEntity(crn: CaseReferenceNumber): ObjectiveEntity = ObjectiveEntity(
    title = title,
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note,
    outcome = outcome,
    caseReferenceNumber = crn,
  )
}
