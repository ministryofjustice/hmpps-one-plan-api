package uk.gov.justice.digital.hmpps.hmppsoneplanapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HmppsOnePlanApi

fun main(args: Array<String>) {
  runApplication<HmppsOnePlanApi>(*args)
}
