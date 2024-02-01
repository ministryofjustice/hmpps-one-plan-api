package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    ],
  )
  @PostMapping("/person/{prisonNumber}/plans/{reference}/objectives")
  suspend fun createObjective(
    @PathVariable(value = "prisonNumber") prisonNumber: String,
    @PathVariable(value = "reference") planReference: UUID,
    @RequestBody request: CreateObjectiveRequest,
  ): CreateEntityResponse {
    val entity = service.createObjective(PlanKey(prisonNumber, planReference), request)
    return CreateEntityResponse(entity.reference)
  }
}
