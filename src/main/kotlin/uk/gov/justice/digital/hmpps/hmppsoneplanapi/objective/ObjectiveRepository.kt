package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ObjectiveRepository : CoroutineCrudRepository<ObjectiveEntity, UUID> {
  @Query(
    """
    select o.* from
        objective o
            join plan_objective_link l
            on o.id = l.objective_id
            and l.plan_id = (
                select id from plan p
                    where p.reference=:planReference
                    and p.crn = :crn
                    and p.is_deleted = false
            )

    where o.reference = :objectiveReference
    and o.is_deleted = false
  """,
  )
  suspend fun getObjective(crn: String, planReference: UUID, objectiveReference: UUID): ObjectiveEntity?

  @Modifying
  @Query(
    """update objective o set is_deleted=true
       where o.reference = :objectiveReference
       and exists(
        select 1 from plan_objective_link l
         where l.objective_id = o.id
         and l.plan_id = (
                select id from plan p
                    where p.reference=:planReference
                    and p.crn = :crn
            )
      )
    """,
  )
  suspend fun markObjectiveDeleted(crn: String, planReference: UUID, objectiveReference: UUID): Int

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
