package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import java.util.UUID

@RestController
@Validated
@Tag(name = "Objective", description = "Manage Objectives")
class ObjectiveController(private val service: ObjectiveService) {

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
  @PostMapping("/person/{crn}/plans/{reference}/objectives")
  suspend fun createObjective(
    @PathVariable(value = "crn") crn: String,
    @PathVariable(value = "reference") planReference: UUID,
    @RequestBody @Valid request: CreateObjectiveRequest,
  ): CreateEntityResponse {
    val entity = service.createObjective(PlanKey(crn, planReference), request)
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
  @GetMapping("/person/{crn}/plans/{planReference}/objectives/{objectiveReference}")
  suspend fun getObjective(
    @PathVariable(value = "crn") crn: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
  ): ObjectiveEntity {
    return service.getObjective(ObjectiveKey(crn, planReference, objectiveReference))
  }

  @Operation(
    summary = "Get all objectives for a Plan",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Objective data is returned, empty array if no objectives found for the Plan",
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
  @GetMapping("/person/{crn}/plans/{planReference}/objectives")
  suspend fun getObjectives(
    @PathVariable(value = "crn") crn: String,
    @PathVariable(value = "planReference") planReference: UUID,
  ): Flow<ObjectiveEntity> {
    return service.getObjectives(PlanKey(crn, planReference))
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
  @PutMapping("/person/{crn}/plans/{planReference}/objectives/{objectiveReference}")
  suspend fun putObjective(
    @PathVariable(value = "crn") crn: String,
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @RequestBody request: UpdateObjectiveRequest,
  ): ObjectiveEntity {
    return service.updateObjective(ObjectiveKey(crn, planReference, objectiveReference), request)
  }

  @Operation(
    summary = "Remove an Objective",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Objective data is removed",
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
  @DeleteMapping("/person/{crn}/plans/{planReference}/objectives/{objectiveReference}")
  suspend fun deleteObjective(
    @PathVariable(value = "planReference") planReference: UUID,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "crn") crn: String,
  ): ResponseEntity<Nothing> {
    service.deleteObjective(ObjectiveKey(crn, planReference, objectiveReference))
    return ResponseEntity.noContent().build()
  }
}
