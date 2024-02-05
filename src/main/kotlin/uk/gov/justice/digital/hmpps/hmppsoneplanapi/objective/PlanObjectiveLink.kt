package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
@Table("plan_objective_link")
data class PlanObjectiveLink(
  val planId: UUID,
  val objectiveId: UUID,
)
