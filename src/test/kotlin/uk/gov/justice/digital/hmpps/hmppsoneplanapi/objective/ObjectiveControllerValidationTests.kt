package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.WebfluxTestBase
import java.util.UUID

@WebFluxTest(controllers = [ObjectiveController::class])
class ObjectiveControllerValidationTests : WebfluxTestBase() {

  @MockkBean
  private lateinit var objectiveService: ObjectiveService

  @TestFactory
  fun `Mandatory string fields`(): Iterable<DynamicTest> {
    val cases = listOf(
      postTest("title", 512) { s -> createRequestBuilder(title = s) },
      postTest("status", 50) { s -> createRequestBuilder(status = s) },
      putTest("title", 512) { s -> updateRequestBuilder(title = s) },
      putTest("status", 50) { s -> updateRequestBuilder(status = s) },
      putTest("reasonForChange", 250) { s -> updateRequestBuilder(reasonForChange = s) },
    )
    return cases.flatMap { case ->
      val (prefix, field, _, jsonBuilder, requestRunner) = case
      listOf(
        dynamicTest("$prefix - 400 when $field is too long") {
          val aLotOfAs = "a".repeat(1000)
          val body = jsonBuilder(aLotOfAs)
          requestRunner(body).value {
            assertThat(it.userMessage).isEqualTo("$field: size must be between 1 and ${case.maxLength}")
          }
        },
        dynamicTest("$prefix - 400 when $field is blank") {
          val body = jsonBuilder("      \n")
          requestRunner(body).value {
            assertThat(it.userMessage).isEqualTo("$field: must not be blank")
          }
        },
        dynamicTest("$prefix - 400 when $field is null") {
          val body = jsonBuilder(null)
          requestRunner(body).value {
            assertThat(it.userMessage).isEqualTo("$field: is required")
          }
        },
      )
    }
  }

  @Test
  fun `Post - 400 when target date is formatted incorrectly`() {
    val body = createRequestBuilder(targetCompletionDate = "not a date")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("targetCompletionDate: should be a date in format yyyy-MM-dd")
    }
  }

  @Test
  fun `Put - 400 when target date is formatted incorrectly`() {
    val body = updateRequestBuilder(targetCompletionDate = "not a date")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("targetCompletionDate: should be a date in format yyyy-MM-dd")
    }
  }

  private fun postTest(field: String, maxLength: Int, jsonBuilder: (String?) -> String): MandatoryStringCase =
    MandatoryStringCase(
      testPrefix = "POST",
      field = field,
      maxLength = maxLength,
      jsonBuilder = jsonBuilder,
      requestRunner = ::post,
    )

  private fun putTest(field: String, maxLength: Int, jsonBuilder: (String?) -> String): MandatoryStringCase =
    MandatoryStringCase(
      testPrefix = "PUT",
      field = field,
      maxLength = maxLength,
      jsonBuilder = jsonBuilder,
      requestRunner = ::put,
    )

  private fun post(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/123/plans/{ref}/objectives", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)

  private fun createRequestBuilder(
    title: String? = "title",
    targetCompletionDate: String? = "2022-02-06",
    status: String? = "status",
    note: String? = "note",
    outcome: String? = "outcome",
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "title" to title,
          "targetCompletionDate" to targetCompletionDate,
          "status" to status,
          "note" to note,
          "outcome" to outcome,
        ).filter { it.value != null },
      )
  }

  private fun put(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.put()
      .uri("/person/123/plans/{pRef}/objectives/{oRef}", UUID.randomUUID(), UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)

  private fun updateRequestBuilder(
    title: String? = "title",
    targetCompletionDate: String? = "2022-02-06",
    status: String? = "status",
    note: String? = "note",
    outcome: String? = "outcome",
    reasonForChange: String? = "reasonForChange",
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "title" to title,
          "targetCompletionDate" to targetCompletionDate,
          "status" to status,
          "note" to note,
          "outcome" to outcome,
          "reasonForChange" to reasonForChange,
        ).filter { it.value != null },
      )
  }
}
data class MandatoryStringCase(
  val testPrefix: String,
  val field: String,
  val maxLength: Int,
  val jsonBuilder: (String?) -> String,
  val requestRunner: (String) -> WebTestClient.BodySpec<ErrorResponse, *>,
)
