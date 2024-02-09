package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import java.util.UUID

@Service
class PlanService(private val planRepository: PlanRepository) {
  suspend fun getByKey(planKey: PlanKey): PlanEntity {
    val (crn, reference) = planKey
    return planRepository.findByCaseReferenceNumberAndReferenceAndIsDeletedIsFalse(crn, reference)
      ?: throw planNotFound(crn, reference)
  }

  suspend fun findAllByCrn(crn: String): Flow<PlanEntity> =
    planRepository.findByCaseReferenceNumberAndIsDeletedIsFalse(crn)

  suspend fun markPlanDeleted(crn: String, reference: UUID) {
    val countUpdated = planRepository.updateMarkDeleted(crn, reference)
    if (countUpdated != 1) {
      throw planNotFound(crn, reference)
    }
  }

  suspend fun createPlan(crn: String, planRequest: CreatePlanRequest): PlanEntity =
    planRepository.save(
      PlanEntity(
        type = planRequest.planType,
        caseReferenceNumber = crn,
      ),
    )
}

fun planNotFound(crn: String, reference: UUID) = NotFoundException("/person/$crn/plans/$reference")
