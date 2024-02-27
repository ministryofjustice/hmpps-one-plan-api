package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.UpdateNotAllowedException
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
  suspend fun createObjective(crn: CaseReferenceNumber, request: CreateObjectiveRequest): ObjectiveEntity {
    val objective = request.buildEntity(crn)
    val savedObjective = entityTemplate.insert(objective).awaitSingle()

    if (request.planReference != null) {
      val plan = planService.getByKey(PlanKey(crn, request.planReference))
      val link = PlanObjectiveLink(planId = plan.id, objectiveId = objective.id)
      entityTemplate.insert(link).awaitSingle()
    }

    return savedObjective
  }

  suspend fun getObjective(objectiveKey: ObjectiveKey): ObjectiveEntity {
    val (crn, objectiveReference) = objectiveKey
    return objectiveRepository.getObjective(crn, objectiveKey.objectiveReference)
      ?: throw NotFoundException("/person/$crn/objectives/$objectiveReference")
  }

  @Transactional
  suspend fun updateObjective(objectiveKey: ObjectiveKey, request: UpdateObjectiveRequest): ObjectiveEntity {
    val objective = getObjective(objectiveKey)
    checkObjectiveCanBeUpdated(objective)
    val updated = request.updateEntity(objective)

    val saveResult = entityTemplate.insert(buildHistory(objective, updated, request.reasonForChange))
      .zipWith(entityTemplate.update(updated))
      .awaitSingle()
    return saveResult.t2
  }

  private fun checkObjectiveCanBeUpdated(objective: ObjectiveEntity) {
    if (objective.status == ObjectiveStatus.COMPLETED) {
      throw UpdateNotAllowedException(ObjectiveEntity::class, objective.reference)
    }
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
    val (crn, objectiveReference) = objectiveKey
    val count = objectiveRepository.markObjectiveDeleted(crn, objectiveReference)
    if (count != 1) {
      throw NotFoundException("/person/$crn/objectives/$objectiveReference")
    }
  }

  suspend fun getObjectives(planKey: PlanKey): Flow<ObjectiveEntity> {
    val plan = planService.getByKey(planKey)
    return objectiveRepository.findAllByPlanId(plan.id)
  }

  suspend fun getObjectives(crn: CaseReferenceNumber): Flow<Objective> {
    return objectiveRepository.findAllByCrn(crn)
      .map { buildObjective(it) }
  }

  suspend fun getObjectivesAndSteps(crn: CaseReferenceNumber): Flow<Objective> {
    return objectiveRepository.findAllByCrnWithSteps(crn).asFlux()
      .groupBy { it.objective.id }
      .flatMap { group ->
        group.collectList()
          .map { stepAndObjectives ->
            buildObjective(
              stepAndObjectives.first().objective,
              stepAndObjectives.mapNotNull { it.step }
                .filter { !it.isDeleted },
            )
          }
      }.asFlow()
  }
}
