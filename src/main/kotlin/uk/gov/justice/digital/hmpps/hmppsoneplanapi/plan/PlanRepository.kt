package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface PlanRepository : CoroutineCrudRepository<PlanEntity, UUID> {
  suspend fun findByPrisonNumberAndReference(prisonNumber: String, reference: UUID): PlanEntity?
}
