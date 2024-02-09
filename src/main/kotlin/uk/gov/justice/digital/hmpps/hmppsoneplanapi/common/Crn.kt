package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.lang.annotation.Inherited
@NotBlank
@Size(min = 1, max = 10)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Crn
