package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono
import java.time.ZonedDateTime
import java.util.Optional

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "auditingDateTimeProvider", auditorAwareRef = "auditorAware")
class AuditingConfig {
  @Bean
  fun auditingDateTimeProvider(): DateTimeProvider {
    // To enable saving with timezone
    return DateTimeProvider { Optional.of(ZonedDateTime.now()) }
  }

  @Bean
  fun auditorAware(): ReactiveAuditorAware<String> = UsernameAuditorAware()
}

class UsernameAuditorAware : ReactiveAuditorAware<String> {
  override fun getCurrentAuditor(): Mono<String> {
    val context = ReactiveSecurityContextHolder.getContext()

    return context.map { c ->
      c.authentication.name
    }
  }
}
