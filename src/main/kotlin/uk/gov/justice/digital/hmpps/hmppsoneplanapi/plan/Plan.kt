package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.Objective
import java.time.ZonedDateTime
import java.util.UUID

data class Plan(
  val reference: UUID,
  val caseReferenceNumber: CaseReferenceNumber,
  val type: PlanType,

  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAtPrison: String?,
  val createdAt: ZonedDateTime?,
  var updatedBy: String?,
  val updatedByDisplayName: String?,
  var updatedAt: ZonedDateTime?,
  val updatedAtPrison: String?,

  @field:JsonInclude(JsonInclude.Include.NON_NULL)
  val objectives: List<Objective>? = null,
)

internal fun buildPlan(planEntity: PlanEntity, objectives: List<Objective>? = null) = Plan(
  reference = planEntity.reference,
  caseReferenceNumber = planEntity.caseReferenceNumber,
  type = planEntity.type,
  createdBy = planEntity.createdBy,
  createdAt = planEntity.createdAt,
  updatedBy = planEntity.updatedBy,
  updatedAt = planEntity.updatedAt,
  objectives = objectives,
  createdByDisplayName = planEntity.createdByDisplayName,
  updatedByDisplayName = planEntity.updatedByDisplayName,
  updatedAtPrison = planEntity.updatedAtPrison,
  createdAtPrison = planEntity.createdAtPrison,
)
