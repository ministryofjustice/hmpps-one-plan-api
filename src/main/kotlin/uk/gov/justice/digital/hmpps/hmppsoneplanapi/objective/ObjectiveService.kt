package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanService

@Service
class ObjectiveService(
  private val planService: PlanService,
  private val entityTemplate: R2dbcEntityTemplate,
  private val objectiveRepository: ObjectiveRepository,
) {
  @Transactional
  suspend fun createObjective(planKey: PlanKey, request: ObjectiveRequest): ObjectiveEntity {
    val plan = planService.getByKey(planKey)
    val objective = request.buildEntity()
    val link = PlanObjectiveLink(planId = plan.id, objectiveId = objective.id)
    val savedObjective = entityTemplate.insert(objective).awaitSingle()
    entityTemplate.insert(link).awaitSingle()
    return savedObjective
  }

  suspend fun getObjective(objectiveKey: ObjectiveKey): ObjectiveEntity {
    val (prisonNumber, planReference, objectiveReference) = objectiveKey
    return objectiveRepository.getObjective(prisonNumber, planReference, objectiveKey.objectiveReference)
      ?: throw NotFoundException("/person/$prisonNumber/plans/$planReference/objectives/$objectiveReference")
  }
  suspend fun updateObjective(objectiveKey: ObjectiveKey, request: ObjectiveRequest): ObjectiveEntity {
    val objective = getObjective(objectiveKey)
    val updated = request.updateEntity(objective)
    return entityTemplate.update(updated).awaitSingle()
  }

  @Transactional
  suspend fun deleteObjective(objectiveKey: ObjectiveKey) {
    val (prisonNumber, planReference, objectiveReference) = objectiveKey
    val count = objectiveRepository.markObjectiveDeleted(prisonNumber, planReference, objectiveReference)
    if (count != 1) {
      throw NotFoundException("/person/$prisonNumber/plans/$planReference/objectives/$objectiveReference")
    }
  }

  suspend fun getObjectives(planKey: PlanKey): Flow<ObjectiveEntity> {
    val plan = planService.getByKey(planKey)
    return objectiveRepository.findAllByPlanId(plan.id)
  }
}
