package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.step.StepEntity
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class Objective(
  @JsonIgnore
  val id: UUID?,
  val reference: UUID,
  val caseReferenceNumber: CaseReferenceNumber,
  val title: String,
  val type: ObjectiveType,
  val targetCompletionDate: LocalDate?,
  val status: ObjectiveStatus,
  val note: String?,
  val outcome: String?,
  val createdBy: String? = null,
  val createdByDisplayName: String? = null,
  val createdAt: ZonedDateTime? = null,
  val updatedBy: String? = createdBy,
  val updatedByDisplayName: String? = createdByDisplayName,
  val updatedAt: ZonedDateTime? = createdAt,
  val createdAtPrison: String? = null,
  val updatedAtPrison: String? = createdAtPrison,

  @field:JsonInclude(JsonInclude.Include.NON_NULL)
  val steps: List<StepEntity>? = null,
)

internal fun buildObjective(entity: ObjectiveEntity, steps: List<StepEntity>? = null): Objective = Objective(
  id = entity.id,
  reference = entity.reference,
  caseReferenceNumber = entity.caseReferenceNumber,
  title = entity.title,
  type = entity.type,
  targetCompletionDate = entity.targetCompletionDate,
  status = entity.status,
  note = entity.note,
  outcome = entity.outcome,
  createdBy = entity.createdBy,
  createdAt = entity.createdAt,
  updatedBy = entity.updatedBy,
  updatedAt = entity.updatedAt,
  createdByDisplayName = entity.createdByDisplayName,
  updatedByDisplayName = entity.updatedByDisplayName,
  createdAtPrison = entity.createdAtPrison,
  updatedAtPrison = entity.updatedAtPrison,
  steps = steps,
)
