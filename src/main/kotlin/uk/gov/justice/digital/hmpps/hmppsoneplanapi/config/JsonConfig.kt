package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import com.fasterxml.jackson.databind.DeserializationFeature
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JsonConfig {
  @Bean
  fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
    return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
      builder.featuresToEnable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    }
  }
}
