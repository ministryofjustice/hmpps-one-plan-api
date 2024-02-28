package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.PlanObjectiveLink
import java.util.UUID

interface PlanRepository : CoroutineCrudRepository<PlanEntity, UUID> {
  @Suppress("SpringDataRepositoryMethodReturnTypeInspection") // PlanEntity? is a valid return type on suspend functions
  suspend fun findByCaseReferenceNumberAndReferenceAndIsDeletedIsFalse(crn: String, reference: UUID): PlanEntity?
  suspend fun findByCaseReferenceNumberAndIsDeletedIsFalse(crn: String): Flow<PlanEntity>

  @Modifying
  @Query("update plan set is_deleted=true where crn = :crn and reference = :reference")
  suspend fun updateMarkDeleted(crn: String, reference: UUID): Int

  @Query(
    """
    select l.* from plan p
     join plan_objective_link l
     on p.id = l.plan_id
     where p.is_deleted = false
     and p.crn = :crn
  """,
  )
  suspend fun findAllPlanLinks(crn: CaseReferenceNumber): Flow<PlanObjectiveLink>
}
