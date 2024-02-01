package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

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
            and l.plan_id = (select id from plan p where p.reference=:planReference and p.prison_number = :prisonNumber)

    where o.reference = :objectiveReference
  """,
  )
  suspend fun getObjective(prisonNumber: String, planReference: UUID, objectiveReference: UUID): ObjectiveEntity?
}
