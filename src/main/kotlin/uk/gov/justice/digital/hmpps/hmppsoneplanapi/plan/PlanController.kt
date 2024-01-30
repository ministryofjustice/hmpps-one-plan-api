package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import java.util.UUID

@RestController
@RequestMapping
class PlanController(val planRepository: PlanRepository) {

  @PostMapping("/person/{prisonNumber}/plans")
  suspend fun createPlan(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @RequestBody planRequest: CreatePlanRequest,
  ): CreatePlanResponse {
    val entity = planRepository.save(
      PlanEntity(
        type = planRequest.planType,
        prisonNumber = prisonNumber,
        createdBy = "TODO",

      ),
    )
    return CreatePlanResponse(entity.reference)
  }

  @GetMapping("/person/{prisonNumber}/plans/{reference}")
  suspend fun getPlan(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "reference") reference: UUID,
  ): PlanEntity? {
    return planRepository.findByPrisonNumberAndReference(prisonNumber, reference)
      ?: throw NotFoundException("/person/$prisonNumber/plans/$reference")
  }

  @GetMapping("/person/{prisonNumber}/plans")
  suspend fun getAllPlans(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
  ): Flow<PlanEntity> {
    return planRepository.findByPrisonNumber(prisonNumber)
  }
}
