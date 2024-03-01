package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.Crn
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Steps", description = "Manage Steps")
@Validated
class StepController(private val service: StepService) {
  @Operation(
    summary = "Create a Step for a given Objective",
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
        description = "Objective not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "418",
        description = "Unique constraint violation, please retry",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PostMapping("/person/{crn}/objectives/{objectiveReference}/steps")
  suspend fun createStep(
    @PathVariable(value = "crn") @NotBlank @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @RequestBody @Valid request: CreateStepRequest,
  ): CreateEntityResponse {
    val entity = service.createStep(ObjectiveKey(crn, objectiveReference), request)
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
        description = "Step or Objective not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping("/person/{crn}/objectives/{objectiveReference}/steps/{stepReference}")
  suspend fun getStep(
    @PathVariable(value = "crn") @NotBlank @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "stepReference") stepReference: UUID,
  ): StepEntity =
    service.getStep(ObjectiveKey(crn, objectiveReference), stepReference)

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
  @GetMapping("/person/{crn}/objectives/{objectiveReference}/steps")
  suspend fun getSteps(
    @PathVariable(value = "crn") @NotBlank @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
  ): Flow<StepEntity> = service.getSteps(ObjectiveKey(crn, objectiveReference))

  @Operation(
    summary = "Remove a Step",
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
        description = "Step or Objective not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @DeleteMapping("/person/{crn}/objectives/{objectiveReference}/steps/{stepReference}")
  suspend fun deleteStep(
    @PathVariable(value = "crn") @NotBlank @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "stepReference") stepReference: UUID,
  ): ResponseEntity<Nothing> {
    service.deleteStep(ObjectiveKey(crn, objectiveReference), stepReference)
    return ResponseEntity.noContent().build()
  }

  @Operation(
    summary = "Updates a single Step",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The result of the update",
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
        description = "Step or Objective not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PutMapping("/person/{crn}/objectives/{objectiveReference}/steps/{stepReference}")
  suspend fun updateStep(
    @PathVariable(value = "crn") @NotBlank @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "stepReference") stepReference: UUID,
    @RequestBody @Valid putStepRequest: PutStepRequest,
  ): StepEntity {
    return service.updateStep(ObjectiveKey(crn, objectiveReference), stepReference, putStepRequest)
  }

  @Operation(
    summary = "Partially updates a single step",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The result of the update",
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
        description = "Step or Objective not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PatchMapping("/person/{crn}/objectives/{objectiveReference}/steps/{stepReference}")
  suspend fun partialUpdateStep(
    @PathVariable(value = "crn") @NotBlank @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "stepReference") stepReference: UUID,
    @RequestBody @Valid patchRequest: PatchStepRequest,
  ): StepEntity {
    return service.updateStep(ObjectiveKey(crn, objectiveReference), stepReference, patchRequest)
  }
}
