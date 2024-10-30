package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.util.UUID

@Component
class AuditService(private val auditService: HmppsAuditService) {
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
  DELETE_PLAN,

  CREATE_OBJECTIVE,
  UPDATE_OBJECTIVE,
  DELETE_OBJECTIVE,

  CREATE_STEP,
  UPDATE_STEP,
  DELETE_STEP,
}
