package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import org.reactivestreams.Publisher
import org.springframework.core.annotation.Order
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

interface DisplayNameAudited {
  var createdByDisplayName: String?
  var updatedByDisplayName: String?
  fun isNew(): Boolean
}

@Component
// This needs to run before spring's inbuilt auditor that sets createdBy etc as it makes a copy and loses the isNew flag
@Order(1)
internal class DisplayNameAudit : BeforeConvertCallback<DisplayNameAudited> {
  override fun onBeforeConvert(entity: DisplayNameAudited, table: SqlIdentifier): Publisher<DisplayNameAudited> {
    return ReactiveSecurityContextHolder.getContext()
      .map { securityContext ->
        val displayName = displayNameFrom(securityContext.authentication.principal)
        if (entity.isNew()) {
          entity.createdByDisplayName = displayName
        }
        entity.updatedByDisplayName = displayName
        entity
      }
  }

  private fun displayNameFrom(principal: Any?): String? =
    when (principal) {
      is Jwt -> principal.getClaim("name")
      else -> null
    }
}
