package uk.gov.justice.digital.hmpps.hmpssoneplanapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HmpssOnePlanApi

fun main(args: Array<String>) {
  runApplication<HmpssOnePlanApi>(*args)
}
