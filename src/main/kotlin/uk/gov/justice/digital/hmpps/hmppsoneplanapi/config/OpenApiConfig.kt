package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
  @Bean
  fun openApi(): OpenAPI = OpenAPI()
    .info(
      Info().title("One Plan API"),
    )
}
