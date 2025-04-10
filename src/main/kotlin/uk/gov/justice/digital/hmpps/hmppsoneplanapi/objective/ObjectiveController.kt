package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.Crn
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import java.util.UUID
import io.swagger.v3.oas.annotations.parameters.RequestBody as BodyDoc

@RestController
@Validated
@Tag(name = "Objective", description = "Manage Objectives")
class ObjectiveController(
  private val objectiveService: ObjectiveService,
) {

  @Operation(
    summary = "Create an Objective",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Objective successfully created, response contains the unique reference that identifies the created Objective",
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
        description = "Plan not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PostMapping("/person/{crn}/objectives")
  suspend fun createObjective(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @RequestBody @Valid request: CreateObjectiveRequest,
  ): CreateEntityResponse {
    val entity = objectiveService.createObjective(crn, request)
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
  @GetMapping("/person/{crn}/objectives/{objectiveReference}")
  suspend fun getObjective(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
  ): ObjectiveEntity = objectiveService.getObjective(ObjectiveKey(crn, objectiveReference))

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
  suspend fun getObjectivesForPlan(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "planReference") planReference: UUID,
  ): Flow<ObjectiveEntity> = objectiveService.getObjectives(PlanKey(crn, planReference))

  @Operation(
    summary = "Get all objectives for a Person",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Objective data is returned, empty array if no objectives found for the Person. " +
          "Steps are only included if includeSteps param is true",
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
  @GetMapping("/person/{crn}/objectives")
  suspend fun getObjectives(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @RequestParam(value = "includeSteps", required = false)
    @Parameter(description = "whether to include the steps of the objective in the response (defaults to false)")
    includeSteps: Boolean = false,
  ): Flow<Objective> = if (includeSteps) {
    objectiveService.getObjectivesAndSteps(crn)
  } else {
    objectiveService.getObjectives(crn)
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
  @PutMapping("/person/{crn}/objectives/{objectiveReference}")
  suspend fun putObjective(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @RequestBody @Valid request: PutObjectiveRequest,
  ): ObjectiveEntity = objectiveService.updateObjective(ObjectiveKey(crn, objectiveReference), request)

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
  @DeleteMapping("/person/{crn}/objectives/{objectiveReference}")
  suspend fun deleteObjective(
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
  ): ResponseEntity<Nothing> {
    objectiveService.deleteObjective(ObjectiveKey(crn, objectiveReference))
    return ResponseEntity.noContent().build()
  }

  @Operation(
    summary = "Partially update data for a single Objective",
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
  @PatchMapping("/person/{crn}/objectives/{objectiveReference}")
  suspend fun patchObjective(
    @PathVariable(value = "crn") @Crn crn: CaseReferenceNumber,
    @PathVariable(value = "objectiveReference") objectiveReference: UUID,
    @RequestBody
    @BodyDoc(
      required = true,
      description = "Set present fields to the given values, only reasonForChange is required",
    )
    @Valid
    request: PatchObjectiveRequest,
  ): ObjectiveEntity = objectiveService.updateObjective(ObjectiveKey(crn, objectiveReference), request)
}
