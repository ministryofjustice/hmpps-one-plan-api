package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate
import java.util.UUID

data class CreateObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val title: String,
  val targetCompletionDate: LocalDate?,
  val status: ObjectiveStatus,
  val note: String?,
  val outcome: String?,
  @field:Schema(description = "Optional plan reference to add objective to")
  val planReference: UUID? = null,
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
