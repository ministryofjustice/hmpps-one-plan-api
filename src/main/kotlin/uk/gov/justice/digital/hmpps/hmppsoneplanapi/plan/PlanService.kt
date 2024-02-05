package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import java.util.UUID

@Service
class PlanService(private val planRepository: PlanRepository) {
  suspend fun getByKey(planKey: PlanKey): PlanEntity {
    val (prisonNumber, reference) = planKey
    return planRepository.findByPrisonNumberAndReferenceAndIsDeletedIsFalse(prisonNumber, reference)
      ?: throw planNotFound(prisonNumber, reference)
  }
}

fun planNotFound(prisonNumber: String, reference: UUID) = NotFoundException("/person/$prisonNumber/plans/$reference")
