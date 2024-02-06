package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveService
import java.time.ZonedDateTime
import java.util.UUID

@Service
class StepService(
  private val objectiveService: ObjectiveService,
  private val entityTemplate: R2dbcEntityTemplate,
  private val stepRepository: StepRepository,
  private val objectMapper: ObjectMapper,
) {
  suspend fun createStep(objectiveKey: ObjectiveKey, request: CreateStepRequest): StepEntity {
    val objective = objectiveService.getObjective(objectiveKey)
    val step = request.buildEntity(objective.id)
    return entityTemplate.insert(step).awaitSingle()
  }

  suspend fun getStep(objectiveKey: ObjectiveKey, stepReference: UUID): StepEntity {
    val objective = objectiveService.getObjective(objectiveKey)
    return stepRepository.findByReferenceAndObjectiveIdAndIsDeletedIsFalse(stepReference, objective.id)
      ?: throw stepNotFound(objectiveKey, stepReference)
  }

  suspend fun getSteps(objectiveKey: ObjectiveKey): Flow<StepEntity> {
    val objective = objectiveService.getObjective(objectiveKey)
    return stepRepository.findAllByObjectiveIdAndIsDeletedIsFalse(objective.id)
  }

  suspend fun deleteStep(objectiveKey: ObjectiveKey, stepReference: UUID) {
    val objective = objectiveService.getObjective(objectiveKey)
    val count = stepRepository.markStepDeleted(objective.id, stepReference)
    if (count != 1) {
      throw stepNotFound(objectiveKey, stepReference)
    }
  }

  suspend fun updateStep(objectiveKey: ObjectiveKey, stepReference: UUID, request: UpdateStepRequest): StepEntity {
    val step = getStep(objectiveKey, stepReference)
    val updated = request.updateEntity(step)
    val result = entityTemplate.insert(buildHistory(step, updated, request.reasonForChange))
      .zipWith(entityTemplate.update(updated)).awaitSingle()
    return result.t2
  }

  private fun buildHistory(original: StepEntity, updated: StepEntity, reasonForChange: String): StepHistory {
    assert(original.id == updated.id) { "Trying to record history for objectives with different id, original: ${original.id}, updated: ${updated.id}" }
    return StepHistory(
      stepId = original.id,
      previousValue = objectMapper.writeValueAsString(original),
      newValue = objectMapper.writeValueAsString(updated),
      reasonForChange = reasonForChange,
      updatedAt = updated.updatedAt ?: ZonedDateTime.now(),
      updatedBy = updated.updatedBy ?: "unknown",
    )
  }
}

fun stepNotFound(objectiveKey: ObjectiveKey, stepReference: UUID): NotFoundException {
  val (crn, planReference, objectiveReference) = objectiveKey
  return NotFoundException(
    "/person/$crn/plans/$planReference/objectives/$objectiveReference/steps/$stepReference",
  )
}
