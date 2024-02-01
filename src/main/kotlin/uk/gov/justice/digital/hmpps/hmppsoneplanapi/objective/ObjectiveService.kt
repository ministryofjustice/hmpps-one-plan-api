package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanService
import java.util.UUID

@Service
class ObjectiveService(
  private val planService: PlanService,
  private val entityTemplate: R2dbcEntityTemplate,
  private val objectiveRepository: ObjectiveRepository,
) {
  @Transactional
  suspend fun createObjective(planKey: PlanKey, request: CreateObjectiveRequest): ObjectiveEntity {
    val plan = planService.getByKey(planKey)
    val objective = request.buildEntity()
    val link = PlanObjectiveLink(planId = plan.id, objectiveId = objective.id)
    val savedObjective = entityTemplate.insert(objective).awaitSingle()
    entityTemplate.insert(link).awaitSingle()
    return savedObjective
  }

  suspend fun getObjective(planKey: PlanKey, objectiveReference: UUID): ObjectiveEntity {
    val (prisonNumber, planReference) = planKey
    return objectiveRepository.getObjective(prisonNumber, planReference, objectiveReference)
      ?: throw NotFoundException("/person/$prisonNumber/plans/$planReference/objectives/$objectiveReference")
  }
}
