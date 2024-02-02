package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import java.time.ZonedDateTime
import java.util.Optional

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class AuditingConfig {
  @Bean
  fun auditingDateTimeProvider(): DateTimeProvider {
    // To enable saving with timezone
    return DateTimeProvider { Optional.of(ZonedDateTime.now()) }
  }
}
