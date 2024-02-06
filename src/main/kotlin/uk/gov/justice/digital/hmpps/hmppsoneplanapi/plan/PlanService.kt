package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

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
}

fun planNotFound(crn: String, reference: UUID) = NotFoundException("/person/$crn/plans/$reference")
