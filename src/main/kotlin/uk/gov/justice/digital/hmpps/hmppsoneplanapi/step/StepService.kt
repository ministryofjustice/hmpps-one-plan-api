package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveService

@Service
class StepService(
  private val objectiveService: ObjectiveService,
  private val entityTemplate: R2dbcEntityTemplate,
) {
  suspend fun createStep(objectiveKey: ObjectiveKey, request: StepRequest): StepEntity {
    val objective = objectiveService.getObjective(objectiveKey)
    val step = request.buildEntity(objective.id)
    return entityTemplate.insert(step).awaitSingle()
  }
}
