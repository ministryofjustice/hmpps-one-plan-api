package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Objective", description = "Manage Objectives")
class StepController(private val service: StepService) {
  @Operation(
    summary = "Create a Step for a given Objective, that's part of a given Plan",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Step successfully created, response contains the unique reference that identifies the created Step",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CreateEntityResponse::class),
          ),
        ],
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
  @PostMapping("/person/{prisonNumber}/plans/{planReference}/objectives/{objectiveReference}/steps")
  suspend fun createStep(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @RequestBody request: StepRequest,
  ): CreateEntityResponse {
    val entity = service.createStep(ObjectiveKey(prisonNumber, planReference, objectiveReference), request)
    return CreateEntityResponse(entity.reference)
  }

  @Operation(
    summary = "Get a single Step",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "",
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
        description = "Step, Objective or Plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/person/{prisonNumber}/plans/{planReference}/objectives/{objectiveReference}/steps/{stepReference}")
  suspend fun getStep(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "stepReference") stepReference: UUID,
  ): StepEntity =
    service.getStep(ObjectiveKey(prisonNumber, planReference, objectiveReference), stepReference)

  @Operation(
    summary = "Get all steps for an objective, empty array if there are none.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "",
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
        description = "Objective or Plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/person/{prisonNumber}/plans/{planReference}/objectives/{objectiveReference}/steps")
  suspend fun getSteps(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
  ): Flow<StepEntity> = service.getSteps(ObjectiveKey(prisonNumber, planReference, objectiveReference))
}
