package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanService
import java.time.ZonedDateTime

@Service
class ObjectiveService(
  private val planService: PlanService,
  private val entityTemplate: R2dbcEntityTemplate,
  private val objectiveRepository: ObjectiveRepository,
  private val objectMapper: ObjectMapper,
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

  suspend fun getObjective(objectiveKey: ObjectiveKey): ObjectiveEntity {
    val (crn, planReference, objectiveReference) = objectiveKey
    return objectiveRepository.getObjective(crn, planReference, objectiveKey.objectiveReference)
      ?: throw NotFoundException("/person/$crn/plans/$planReference/objectives/$objectiveReference")
  }

  @Transactional
  suspend fun updateObjective(objectiveKey: ObjectiveKey, request: UpdateObjectiveRequest): ObjectiveEntity {
    val objective = getObjective(objectiveKey)
    val updated = request.updateEntity(objective)

    val saveResult = entityTemplate.insert(buildHistory(objective, updated, request.reasonForChange))
      .zipWith(entityTemplate.update(updated))
      .awaitSingle()
    return saveResult.t2
  }

  private fun buildHistory(original: ObjectiveEntity, updated: ObjectiveEntity, reasonForChange: String): ObjectiveHistory {
    assert(original.id == updated.id) { "Trying to record history for objectives with different id, original: ${original.id}, updated: ${updated.id}" }
    return ObjectiveHistory(
      objectiveId = original.id,
      previousValue = objectMapper.writeValueAsString(original),
      newValue = objectMapper.writeValueAsString(updated),
      reasonForChange = reasonForChange,
      updatedAt = updated.updatedAt ?: ZonedDateTime.now(),
      updatedBy = updated.updatedBy ?: "unknown",
    )
  }

  @Transactional
  suspend fun deleteObjective(objectiveKey: ObjectiveKey) {
    val (crn, planReference, objectiveReference) = objectiveKey
    val count = objectiveRepository.markObjectiveDeleted(crn, planReference, objectiveReference)
    if (count != 1) {
      throw NotFoundException("/person/$crn/plans/$planReference/objectives/$objectiveReference")
    }
  }

  suspend fun getObjectives(planKey: PlanKey): Flow<ObjectiveEntity> {
    val plan = planService.getByKey(planKey)
    return objectiveRepository.findAllByPlanId(plan.id)
  }
}
