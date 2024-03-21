package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.assertj.core.api.AbstractAssert
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

class IsFullyPopulatedAssert<T>(
  actual: T,
) : AbstractAssert<IsFullyPopulatedAssert<T>, T>(actual, IsFullyPopulatedAssert::class.java) {
  private val ignored: MutableSet<String> = mutableSetOf()

  fun excluding(vararg propertyNames: String): IsFullyPopulatedAssert<T> {
    ignored.addAll(propertyNames)
    return this
  }

  fun isFullyPopulated() {
    if (actual == null) {
      throw AssertionError("Expected actual to be non-null")
    }
    val nullProps = actual!!::class.declaredMemberProperties.filter { it.visibility == KVisibility.PUBLIC }
      .mapNotNull { prop ->
        val value = prop.call(actual)
        if (value == null && prop.name !in ignored) {
          prop.name
        } else {
          null
        }
      }
    if (nullProps.isNotEmpty()) {
      throw AssertionError("Expected properties ${nullProps.joinToString()} to be non-null")
    }
  }
}

fun <T> assertThatPop(actual: T) = IsFullyPopulatedAssert(actual)
