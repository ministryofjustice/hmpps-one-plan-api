package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.base.url")
data class ApiUrlProperties(val oauth: String)
