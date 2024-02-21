package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import java.util.UUID

@Service
class PlanService(private val planRepository: PlanRepository) {
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

  suspend fun createPlan(crn: CaseReferenceNumber, planRequest: CreatePlanRequest): PlanEntity =
    planRepository.save(
      PlanEntity(
        type = planRequest.planType,
        caseReferenceNumber = crn.value,
      ),
    )
}

fun planNotFound(crn: CaseReferenceNumber, reference: UUID) = NotFoundException("/person/$crn/plans/$reference")
