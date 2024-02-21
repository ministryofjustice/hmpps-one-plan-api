package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import java.util.UUID

data class AddObjectivesRequest(
  val objectives: List<UUID>,
)
