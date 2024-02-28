package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveEntity
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveRepository
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.PlanObjectiveLink
import java.util.UUID

@Service
class PlanService(
  private val planRepository: PlanRepository,
  private val objectiveRepository: ObjectiveRepository,
  private val entityTemplate: R2dbcEntityTemplate,
) {
  suspend fun getByKey(planKey: PlanKey): PlanEntity {
    val (crn, reference) = planKey
    return planRepository.findByCaseReferenceNumberAndReferenceAndIsDeletedIsFalse(crn.value, reference)
      ?: throw planNotFound(crn, reference)
  }

  suspend fun findAllByCrn(crn: CaseReferenceNumber): Flow<PlanEntity> =
    planRepository.findByCaseReferenceNumberAndIsDeletedIsFalse(crn.value)

  suspend fun markPlanDeleted(crn: CaseReferenceNumber, reference: UUID) {
    val countUpdated = planRepository.updateMarkDeleted(crn.value, reference)
    if (countUpdated != 1) {
      throw planNotFound(crn, reference)
    }
  }

  @Transactional
  suspend fun createPlan(crn: CaseReferenceNumber, planRequest: CreatePlanRequest): PlanEntity {
    val createdPlan = planRepository.save(
      PlanEntity(
        type = planRequest.planType,
        caseReferenceNumber = crn,
      ),
    )

    getObjectives(crn, planRequest.objectives).map { objective ->
      createPlanObjectiveLink(createdPlan, objective)
    }.collect()

    return createdPlan
  }

  private suspend fun createPlanObjectiveLink(
    plan: PlanEntity,
    objective: ObjectiveEntity,
  ): PlanObjectiveLink? {
    val link = PlanObjectiveLink(planId = plan.id, objectiveId = objective.id)
    return entityTemplate.insert(link).awaitSingle()
  }

  suspend fun getObjectives(crn: CaseReferenceNumber, references: Collection<UUID>): Flow<ObjectiveEntity> {
    if (references.isEmpty()) {
      return emptyFlow()
    }

    val foundObjectives = objectiveRepository.getObjectives(crn, references)

    if (foundObjectives.count() != references.size) {
      val foundRefs = foundObjectives.map { it.reference }.toSet()
      val firstNotFound = references.first { it !in foundRefs }
      throw objectiveNotFound(crn, firstNotFound)
    }

    return foundObjectives
  }

  @Transactional
  suspend fun addObjectives(planKey: PlanKey, objectiveRefs: List<UUID>) {
    val plan = getByKey(planKey)

    objectiveRefs.asFlow()
      .map { ref ->
        val objective = objectiveRepository.getObjective(planKey.crn, ref) ?: throw objectiveNotFound(planKey.crn, ref)
        createPlanObjectiveLink(plan, objective)
      }.collect()
  }

  suspend fun findAllLinksByCrn(crn: CaseReferenceNumber): Flow<PlanObjectiveLink> = planRepository.findAllPlanLinks(crn)
}

fun planNotFound(crn: CaseReferenceNumber, reference: UUID) = NotFoundException("/person/$crn/plans/$reference")
fun objectiveNotFound(crn: CaseReferenceNumber, ref: UUID) = NotFoundException("/person/$crn/objectives/$ref")
