package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.step.StepEntity
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class Objective(
  val reference: UUID,
  val caseReferenceNumber: CaseReferenceNumber,
  val title: String,
  val targetCompletionDate: LocalDate?,
  val status: ObjectiveStatus,
  val note: String?,
  val outcome: String?,
  val createdBy: String? = null,
  val createdAt: ZonedDateTime? = null,
  val updatedBy: String? = createdBy,
  val updatedAt: ZonedDateTime? = createdAt,

  @JsonInclude(JsonInclude.Include.NON_NULL)
  val steps: List<StepEntity>? = null,
)

internal fun buildObjective(entity: ObjectiveEntity, steps: List<StepEntity>? = null): Objective = Objective(
  reference = entity.reference,
  caseReferenceNumber = entity.caseReferenceNumber,
  title = entity.title,
  targetCompletionDate = entity.targetCompletionDate,
  status = entity.status,
  note = entity.note,
  outcome = entity.outcome,
  createdBy = entity.createdBy,
  createdAt = entity.createdAt,
  updatedBy = entity.updatedBy,
  updatedAt = entity.updatedAt,
  steps = steps,
)