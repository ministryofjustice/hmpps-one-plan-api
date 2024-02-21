package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.Crn
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Plan", description = "Manage plans")
@Validated
class PlanController(private val planService: PlanService) {

  @Operation(
    summary = "Create a Plan for the person identified by the given CRN (Case Reference Number)",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Plan successfully created, response contains the unique reference that identifies the created Plan",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateEntityResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to use this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "One of the objectives given in the body does not exist",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PostMapping("/person/{crn}/plans")
  suspend fun createPlan(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @RequestBody planRequest: CreatePlanRequest,
  ): CreateEntityResponse {
    val entity = planService.createPlan(crn, planRequest)
    return CreateEntityResponse(entity.reference)
  }

  @Operation(
    summary = "Get data for a single Plan",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Plan data is returned",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to use this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/person/{crn}/plans/{planReference}")
  suspend fun getPlan(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "planReference") planReference: UUID,
  ): PlanEntity? {
    return planService.getByKey(PlanKey(crn, planReference))
  }

  @Operation(
    summary = "Get data for all plans for a given person",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "An array containing all plans for the given person. Will be empty if no plans are found",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to use this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/person/{crn}/plans")
  suspend fun getAllPlans(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
  ): Flow<PlanEntity> {
    return planService.findAllByCrn(crn)
  }

  @Operation(
    summary = "Delete a single plan",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Plan marked as deleted",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to use this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @DeleteMapping("/person/{crn}/plans/{planReference}")
  suspend fun deletePlan(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "planReference") reference: UUID,
  ): ResponseEntity<Nothing> {
    planService.markPlanDeleted(crn, reference)
    return ResponseEntity.noContent().build()
  }

  @Operation(
    summary = "Adds an existing objective to a plan",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Objective added to plan",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to use this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Plan or Objective not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PatchMapping("/person/{crn}/plans/{planReference}/objectives")
  suspend fun addObjectives(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "planReference") reference: UUID,
    @RequestBody addObjectiveRequest: AddObjectivesRequest,
  ): ResponseEntity<Nothing> {
    planService.addObjectives(PlanKey(crn, reference), addObjectiveRequest.objectives)
    return ResponseEntity.noContent().build()
  }
}
