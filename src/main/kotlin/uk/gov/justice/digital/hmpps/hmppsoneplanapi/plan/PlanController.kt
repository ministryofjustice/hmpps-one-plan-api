package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class PlanController(val planRepository: PlanRepository) {

  @PostMapping
  @RequestMapping("/person/{prisonNumber}/plans")
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
}
