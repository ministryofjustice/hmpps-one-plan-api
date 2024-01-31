package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface PlanRepository : CoroutineCrudRepository<PlanEntity, UUID> {
  @Suppress("SpringDataRepositoryMethodReturnTypeInspection") // PlanEntity? is a valid return type on suspend functions
  suspend fun findByPrisonNumberAndReferenceAndIsDeletedIsFalse(prisonNumber: String, reference: UUID): PlanEntity?
  suspend fun findByPrisonNumberAndIsDeletedIsFalse(prisonNumber: String): Flow<PlanEntity>

  @Modifying
  @Query("update plan set is_deleted=true where prison_number = :prisonNumber and reference = :reference")
  suspend fun updateMarkDeleted(prisonNumber: String, reference: UUID): Int
}
