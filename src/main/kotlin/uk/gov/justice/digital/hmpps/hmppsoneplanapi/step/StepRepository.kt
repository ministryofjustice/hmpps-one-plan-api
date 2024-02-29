package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface StepRepository : CoroutineCrudRepository<StepEntity, UUID> {
  suspend fun findByReferenceAndObjectiveIdAndIsDeletedIsFalse(reference: UUID, objectiveId: UUID): StepEntity?

  suspend fun findAllByObjectiveIdAndIsDeletedIsFalseOrderByStepOrder(objectiveId: UUID): Flow<StepEntity>

  @Modifying
  @Query(
    """
      update step s
        set is_deleted=true, status='ARCHIVED'
        where reference = :stepReference
        and objective_id = :objectiveId
    """,
  )
  suspend fun markStepDeleted(
    objectiveId: UUID,
    stepReference: UUID,
  ): Int

  @Query(
    """
      select coalesce((select max(step_order) + 1 from step where objective_id = :objectiveId), 1)
    """,
  )
  suspend fun nextStepId(objectiveId: UUID): Int
}
