package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveService

@Service
class LinkService(
  private val planService: PlanService,
  private val objectiveService: ObjectiveService,
) {

  suspend fun getAllPlansWithObjectivesAndSteps(crn: CaseReferenceNumber): Flow<Plan> {
    return coroutineScope {
      val plans = async { planService.findAllByCrn(crn) }
      val objectives = async {
        objectiveService.getObjectivesAndSteps(crn).toList().associateBy { it.id }
      }
      val links = async { planService.findAllLinksByCrn(crn).toList().groupBy { it.planId } }

      val objectivesById = objectives.await()
      val linksByPlanId = links.await()
      plans.await().map { planEntity ->
        val planObjectives = linksByPlanId[planEntity.id].orEmpty().mapNotNull { link ->
          objectivesById[link.objectiveId]
        }
        buildPlan(planEntity, planObjectives)
      }
    }
  }
}
