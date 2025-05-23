package uk.gov.justice.digital.hmpps.hmppsoneplanapi.common

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@JvmInline
value class CaseReferenceNumber(
  val value: String,
) {
  override fun toString(): String = value
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
@Constraint(validatedBy = [CrnValidator::class])
@Parameter(description = "The case reference number of the person")
annotation class Crn(
  val message: String = "crn: must be not blank and between and no more than 10 characters",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class CrnValidator : ConstraintValidator<Crn, String> {
  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return false
    }

    return value.isNotBlank() && value.length <= 10
  }
}
