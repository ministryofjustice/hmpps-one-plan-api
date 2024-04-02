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
      update objective o set is_deleted=true, status='ARCHIVED'
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

  @Query(
    """
    select
        s.id as step_id,
        s.reference as step_reference,
        s.description as step_description,
        s.step_order,
        s.staff_note,
        s.staff_task,
        s.status as step_status,
        s.created_at as step_created_at,
        s.created_by as step_created_by,
        s.created_by_display_name as step_created_by_display_name,
        s.updated_at as step_updated_at,
        s.updated_by as step_updated_by,
        s.updated_by as step_updated_by_display_name,
        s.created_at_prison as step_created_at_prison,
        s.updated_at_prison as step_updated_at_prison,

        o.id as objective_id,
        o.reference as objective_reference,
        o.crn,
        o.type as objective_type,
        o.title as objective_title,
        o.status as objective_status,
        o.note,
        o.outcome,
        o.target_completion_date,
        o.created_at as objective_created_at,
        o.created_by as objective_created_by,
        o.created_by_display_name as objective_created_by_display_name,
        o.updated_at as objective_updated_at,
        o.updated_by as objective_updated_by,
        o.updated_by_display_name as objective_updated_by_display_name,
        o.created_at_prison as objective_created_at_prison,
        o.updated_at_prison as objective_updated_at_prison
    from objective o
    left outer join step s
        on o.id = s.objective_id
    where o.crn = :caseReferenceNumber
    and o.is_deleted = false
    order by o.created_at, o.id, s.step_order
  """,
  )
  suspend fun findAllByCrnWithSteps(crn: CaseReferenceNumber): Flow<ObjectiveAndStep>

  @Query(
    """
    select * from objective
    where crn = :crn
    and is_deleted = false
  """,
  )
  suspend fun findAllByCrn(crn: CaseReferenceNumber): Flow<ObjectiveEntity>

  @Query(
    """
    select * from objective
    where reference = :objectiveReference
    and crn = :crn
    and is_deleted = false
    for update
  """,
  )
  suspend fun getObjectiveForUpdate(crn: CaseReferenceNumber, objectiveReference: UUID): ObjectiveEntity?
}
