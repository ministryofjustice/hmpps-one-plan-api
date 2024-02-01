package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Objective", description = "Manage Objectives")
class ObjectiveController(val service: ObjectiveService) {

  @Operation(
    summary = "Create an Objective for a given plan",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Objective successfully created, response contains the unique reference that identifies the created Objective",
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
        description = "Plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PostMapping("/person/{prisonNumber}/plans/{reference}/objectives")
  suspend fun createObjective(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "reference") planReference: UUID,
    @RequestBody request: ObjectiveRequest,
  ): CreateEntityResponse {
    val entity = service.createObjective(PlanKey(prisonNumber, planReference), request)
    return CreateEntityResponse(entity.reference)
  }

  @Operation(
    summary = "Get data for a single Objective",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Objective data is returned",
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
        description = "Objective or plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/person/{prisonNumber}/plans/{planReference}/objectives/{objectiveReference}")
  suspend fun getObjective(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
  ): ObjectiveEntity {
    return service.getObjective(PlanKey(prisonNumber, planReference), objectiveReference)
  }

  @Operation(
    summary = "Update data for a single Objective",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Objective data is returned",
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
        description = "Objective or plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PutMapping("/person/{prisonNumber}/plans/{planReference}/objectives/{objectiveReference}")
  suspend fun putObjective(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @RequestBody request: ObjectiveRequest,
  ): ObjectiveEntity {
    return service.updateObjective(PlanKey(prisonNumber, planReference), objectiveReference, request)
  }
}