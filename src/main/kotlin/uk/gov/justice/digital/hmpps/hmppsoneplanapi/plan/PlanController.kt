package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Plan", description = "Manage plans")
class PlanController(private val planRepository: PlanRepository, private val planService: PlanService) {

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
    ],
  )
  @PostMapping("/person/{crn}/plans")
  suspend fun createPlan(
    @PathVariable(value = "crn") crn: String,
    @RequestBody planRequest: CreatePlanRequest,
  ): CreateEntityResponse {
    val entity = planRepository.save(
      PlanEntity(
        type = planRequest.planType,
        caseReferenceNumber = crn,
      ),
    )
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
  @GetMapping("/person/{crn}/plans/{reference}")
  suspend fun getPlan(
    @PathVariable(value = "crn") crn: String,
    @PathVariable(value = "reference") reference: UUID,
  ): PlanEntity? {
    return planService.getByKey(PlanKey(crn, reference))
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
    @PathVariable(value = "crn") crn: String,
  ): Flow<PlanEntity> {
    return planRepository.findByCaseReferenceNumberAndIsDeletedIsFalse(crn)
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
  @DeleteMapping("/person/{crn}/plans/{reference}")
  suspend fun deletePlan(
    @PathVariable(value = "crn") crn: String,
    @PathVariable(value = "reference") reference: UUID,
  ): ResponseEntity<Nothing> {
    val countUpdated = planRepository.updateMarkDeleted(crn, reference)
    if (countUpdated != 1) {
      throw planNotFound(crn, reference)
    }
    return ResponseEntity.noContent().build()
  }
}
