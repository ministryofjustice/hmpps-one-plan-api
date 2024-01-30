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
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.NotFoundException
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Plan", description = "Manage plans")
class PlanController(val planRepository: PlanRepository) {

  @Operation(
    summary = "Create a Plan for the person identified by the given prison number",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Plan succesfully created, response contains the unique reference that identifies the created Plan",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CreatePlanResponse::class))],
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
  @GetMapping("/person/{prisonNumber}/plans/{reference}")
  suspend fun getPlan(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "reference") reference: UUID,
  ): PlanEntity? {
    return planRepository.findByPrisonNumberAndReferenceAndIsDeletedIsFalse(prisonNumber, reference)
      ?: throw planNotFound(prisonNumber, reference)
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
  @GetMapping("/person/{prisonNumber}/plans")
  suspend fun getAllPlans(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
  ): Flow<PlanEntity> {
    return planRepository.findByPrisonNumberAndIsDeletedIsFalse(prisonNumber)
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
  @DeleteMapping("/person/{prisonNumber}/plans/{reference}")
  suspend fun deletePlan(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "reference") reference: UUID,
  ): ResponseEntity<Nothing> {
    val countUpdated = planRepository.updateMarkDeleted(prisonNumber, reference)
    if (countUpdated != 1) {
      throw planNotFound(prisonNumber, reference)
    }
    return ResponseEntity.noContent().build()
  }

  fun planNotFound(prisonNumber: String, reference: UUID) = NotFoundException("/person/$prisonNumber/plans/$reference")
}
