package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface PlanRepository : CoroutineCrudRepository<PlanEntity, UUID> {
  @Suppress("SpringDataRepositoryMethodReturnTypeInspection") // PlanEntity? is a valid return type on suspend functions
  suspend fun findByPrisonNumberAndReference(prisonNumber: String, reference: UUID): PlanEntity?
  suspend fun findByPrisonNumber(prisonNumber: String): Flow<PlanEntity>
}
