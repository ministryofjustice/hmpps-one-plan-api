package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreatePlanRequest(
  val planType: PlanType,
  @field:Schema(requiredMode = NOT_REQUIRED, description = "Optionally add these objectives to the created plan")
  val objectives: Collection<UUID> = emptyList(),
  @field:Size(min = 0, max = 250)
  val createdAtPrison: String? = null,
)
