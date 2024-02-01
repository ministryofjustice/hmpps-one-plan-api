package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface PlanObjectiveLinkRepository : CoroutineCrudRepository<ObjectiveEntity, UUID>
