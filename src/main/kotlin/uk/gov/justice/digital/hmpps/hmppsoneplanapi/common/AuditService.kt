package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.context.annotation.Import
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppsauditsdk.AuditService as AuditSdkService

@Component
@Import(AuditSdkService::class)
class AuditService(private val auditService: AuditSdkService) {
  suspend fun audit(what: AuditAction, crn: CaseReferenceNumber, reference: UUID) {
    val authentication = ReactiveSecurityContextHolder.getContext().map { it.authentication }.awaitSingle()

    auditService.publishEvent(
      what = what.name,
      who = authentication.name,
      subjectId = crn.value,
      subjectType = "USER_ID",
      correlationId = reference.toString(),
      service = "hmpps-one-plan-api",
    )
  }
}

enum class AuditAction {
  CREATE_PLAN,
  UPDATE_PLAN,
  DELETE_PLAN,

  CREATE_OBJECTIVE,
  UPDATE_OBJECTIVE,
  DELETE_OBJECTIVE,

  CREATE_STEP,
  UPDATE_STEP,
  DELETE_STEP,
}
