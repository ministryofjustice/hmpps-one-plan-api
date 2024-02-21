package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class AddObjectivesRequest(
  @field:NotEmpty
  val objectives: List<UUID>,
)
