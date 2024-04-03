package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.sanitise
import java.time.LocalDate
import java.util.UUID

data class CreateObjectiveRequest(
  @field:NotBlank
  @field:Size(min = 1, max = 512)
  val title: String,
  val type: ObjectiveType,
  val status: ObjectiveStatus,
  val targetCompletionDate: LocalDate? = null,
  val note: String? = null,
  val outcome: String? = null,
  @field:Schema(description = "Optional plan reference to add objective to")
  val planReference: UUID? = null,
  @field:Size(min = 0, max = 250)
  val createdAtPrison: String? = null,
) {
  fun buildEntity(crn: CaseReferenceNumber): ObjectiveEntity = ObjectiveEntity(
    title = title.sanitise(),
    type = type,
    targetCompletionDate = targetCompletionDate,
    status = status,
    note = note?.sanitise(),
    outcome = outcome?.sanitise(),
    caseReferenceNumber = crn,
    createdAtPrison = createdAtPrison?.sanitise(),
  )
}
