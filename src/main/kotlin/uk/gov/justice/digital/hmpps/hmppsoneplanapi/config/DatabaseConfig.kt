package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.StepAndObjectiveConverter

@Configuration
class DatabaseConfig : AbstractR2dbcConfiguration() {
  override fun connectionFactory(): ConnectionFactory {
    return ConnectionFactories.get("r2dbc:…")
  }

  override fun getCustomConverters(): List<Any> {
    return listOf(StepAndObjectiveConverter())
  }
}
