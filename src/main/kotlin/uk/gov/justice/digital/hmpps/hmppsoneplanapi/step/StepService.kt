package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveService
import java.util.UUID

@Service
class StepService(
  private val objectiveService: ObjectiveService,
  private val entityTemplate: R2dbcEntityTemplate,
  private val stepRepository: StepRepository,
) {
  suspend fun createStep(objectiveKey: ObjectiveKey, request: StepRequest): StepEntity {
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
}

fun stepNotFound(objectiveKey: ObjectiveKey, stepReference: UUID): NotFoundException {
  val (prisonNumber, planReference, objectiveReference) = objectiveKey
  return NotFoundException(
    "/person/$prisonNumber/plans/$planReference/objectives/$objectiveReference/steps/$stepReference",
  )
}
