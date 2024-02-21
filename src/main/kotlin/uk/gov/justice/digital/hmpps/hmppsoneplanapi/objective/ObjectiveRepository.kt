package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.util.UUID

interface ObjectiveRepository : CoroutineCrudRepository<ObjectiveEntity, UUID> {
  @Query(
    """
    select * from objective
    where reference = :objectiveReference
    and crn = :crn
    and is_deleted = false
  """,
  )
  suspend fun getObjective(crn: CaseReferenceNumber, objectiveReference: UUID): ObjectiveEntity?

  @Query(
    """
    select * from objective
    where reference in (:objectiveReferences)
    and crn = :crn
    and is_deleted = false
  """,
  )
  suspend fun getObjectives(crn: CaseReferenceNumber, objectiveReferences: Collection<UUID>): Flow<ObjectiveEntity>

  @Modifying
  @Query(
    """
      update objective o set is_deleted=true
      where o.reference = :objectiveReference
        and o.crn = :crn
        and o.is_deleted = false
    """,
  )
  suspend fun markObjectiveDeleted(crn: CaseReferenceNumber, objectiveReference: UUID): Int

  @Query(
    """
      select o.* from objective o
          join plan_objective_link l
            on o.id = l.objective_id
            and l.plan_id = :id
          join plan p
            on l.plan_id = p.id
      where p.is_deleted = false
    """,
  )
  suspend fun findAllByPlanId(id: UUID): Flow<ObjectiveEntity>
}
