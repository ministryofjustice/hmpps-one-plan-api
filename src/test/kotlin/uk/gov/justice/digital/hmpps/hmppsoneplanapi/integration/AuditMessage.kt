package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath

private val whatPath = JsonPath.compile("$.what")
private val correlationIdPath = JsonPath.compile("$.correlationId")
private val jsonProvider = Configuration.defaultConfiguration().jsonProvider()

class AuditMessage(private val message: String) {
  private val document = jsonProvider.parse(message)

  val what: String
    get() = whatPath.read(document)

  val correlationId: String
    get() = correlationIdPath.read(document)

  override fun toString(): String = "AuditMessage($message)"
}
