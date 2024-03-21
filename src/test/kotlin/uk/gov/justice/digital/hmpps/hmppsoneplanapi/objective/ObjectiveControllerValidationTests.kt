package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.WebfluxTestBase
import java.util.UUID

@WebFluxTest(controllers = [ObjectiveController::class])
class ObjectiveControllerValidationTests : WebfluxTestBase() {

  @MockkBean
  private lateinit var objectiveService: ObjectiveService

  @Test
  fun `Post - 400 when title field is too long`() {
    val body = createRequestBuilder(title = "A".repeat(513))
    post(body).value {
      assertThat(it.userMessage).isEqualTo("title: size must be between 1 and 512")
    }
  }

  @Test
  fun `Post - 400 when title field is null`() {
    val body = createRequestBuilder(title = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("title: is required")
    }
  }

  @Test
  fun `Post - 400 when title field is blank`() {
    val body = createRequestBuilder(title = "\n   ")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("title: must not be blank")
    }
  }

  @Test
  fun `Post - 400 when status type is missing`() {
    post(createRequestBuilder(status = null))
      .value { assertThat(it.userMessage).isEqualTo("status: is required") }
  }

  @Test
  fun `Post - 400 when status is not one of the allowed values`() {
    post(createRequestBuilder(status = "BATMAN")).value {
      assertThat(it.userMessage)
        .isEqualTo("status: must be one of [NOT_STARTED, BLOCKED, DEFERRED, IN_PROGRESS, COMPLETED, ARCHIVED]")
    }
  }

  @Test
  fun `Post - 400 when target date is formatted incorrectly`() {
    val body = createRequestBuilder(targetCompletionDate = "not a date")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("targetCompletionDate: must be a date in format yyyy-MM-dd")
    }
  }

  @Test
  fun `Post - 400 when crn too long`() {
    val body = createRequestBuilder()
    post(body, crn = "1".repeat(11)).value {
      assertThat(it.userMessage).isEqualTo("crn: must be not blank and between and no more than 10 characters")
    }
  }

  @Test
  fun `Post - 400 when created at prison is too long`() {
    post(createRequestBuilder(createdAtPrison = "1".repeat(251)))
      .value { assertThat(it.userMessage).isEqualTo("createdAtPrison: size must be between 0 and 250") }
  }

  @Test
  fun `Put - 400 when target date is formatted incorrectly`() {
    val body = updateRequestBuilder(targetCompletionDate = "not a date")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("targetCompletionDate: must be a date in format yyyy-MM-dd")
    }
  }

  @Test
  fun `Put - 400 when status type is missing`() {
    put(updateRequestBuilder(status = null))
      .value { assertThat(it.userMessage).isEqualTo("status: is required") }
  }

  @Test
  fun `Put - 400 when status is not one of the allowed values`() {
    put(updateRequestBuilder(status = "BATMAN")).value {
      assertThat(it.userMessage)
        .isEqualTo("status: must be one of [NOT_STARTED, BLOCKED, DEFERRED, IN_PROGRESS, COMPLETED, ARCHIVED]")
    }
  }

  @Test
  fun `Put - 400 when title field is too long`() {
    val body = updateRequestBuilder(title = "Z".repeat(513))
    put(body).value {
      assertThat(it.userMessage).isEqualTo("title: size must be between 1 and 512")
    }
  }

  @Test
  fun `Put - 400 when title field is null`() {
    val body = updateRequestBuilder(title = null)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("title: is required")
    }
  }

  @Test
  fun `Put - 400 when title field is blank`() {
    val body = updateRequestBuilder(title = "\n   ")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("title: must not be blank")
    }
  }

  @Test
  fun `Put - 400 when reasonForChange field is too long`() {
    val body = updateRequestBuilder(reasonForChange = "X".repeat(251))
    put(body).value {
      assertThat(it.userMessage).isEqualTo("reasonForChange: size must be between 1 and 250")
    }
  }

  @Test
  fun `Put - 400 when reasonForChange field is null`() {
    val body = updateRequestBuilder(reasonForChange = null)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("reasonForChange: is required")
    }
  }

  @Test
  fun `Put - 400 when reasonForChange field is blank`() {
    val body = updateRequestBuilder(reasonForChange = "\n   ")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("reasonForChange: must not be blank")
    }
  }

  @Test
  fun `Put - 400 when created at prison is too long`() {
    put(updateRequestBuilder(updatedAtPrison = "1".repeat(251)))
      .value { assertThat(it.userMessage).isEqualTo("updatedAtPrison: size must be between 0 and 250") }
  }

  private fun post(body: String, crn: String = "123"): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/{crn}/objectives", crn, UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)

  private fun createRequestBuilder(
    title: String? = "title",
    targetCompletionDate: String? = "2022-02-06",
    status: String? = "IN_PROGRESS",
    note: String? = "note",
    outcome: String? = "outcome",
    createdAtPrison: String? = null,
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "title" to title,
          "targetCompletionDate" to targetCompletionDate,
          "status" to status,
          "note" to note,
          "outcome" to outcome,
          "createdAtPrison" to createdAtPrison,
        ).filter { it.value != null },
      )
  }

  private fun put(body: String): WebTestClient.BodySpec<ErrorResponse, *> = request(HttpMethod.PUT, body)
  private fun patch(body: String): WebTestClient.BodySpec<ErrorResponse, *> = request(HttpMethod.PATCH, body)

  private fun request(method: HttpMethod, body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.method(method)
      .uri("/person/123/objectives/{oRef}", UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)

  private fun updateRequestBuilder(
    title: String? = "title",
    targetCompletionDate: String? = "2022-02-06",
    status: String? = "IN_PROGRESS",
    note: String? = "note",
    outcome: String? = "outcome",
    reasonForChange: String? = "reasonForChange",
    updatedAtPrison: String? = null,
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
          "updatedAtPrison" to updatedAtPrison,
        ).filter { it.value != null },
      )
  }

  private fun patchRequestBuilder(
    title: String? = null,
    targetCompletionDate: String? = null,
    status: String? = null,
    note: String? = null,
    outcome: String? = null,
    reasonForChange: String? = "reasonable",
    updatedAtPrison: String? = null,
  ) = updateRequestBuilder(title, targetCompletionDate, status, note, outcome, reasonForChange, updatedAtPrison)

  @Test
  fun `Patch - 400 when reasonForChange field is too long`() {
    val body = updateRequestBuilder(title = "something", reasonForChange = "X".repeat(251))
    patch(body).value {
      assertThat(it.userMessage).isEqualTo("reasonForChange: size must be between 1 and 250")
    }
  }

  @Test
  fun `Patch - 400 when reasonForChange field is null`() {
    val body = patchRequestBuilder(title = "something", reasonForChange = null)
    patch(body).value {
      assertThat(it.userMessage).isEqualTo("reasonForChange: is required")
    }
  }

  @Test
  fun `Patch - 400 when reasonForChange field is blank`() {
    val body = patchRequestBuilder(title = "something", reasonForChange = "\n   ")
    patch(body).value {
      assertThat(it.userMessage).isEqualTo("reasonForChange: must not be blank")
    }
  }

  @Test
  fun `Patch - 400 when title field is too long`() {
    val body = patchRequestBuilder(title = "Z".repeat(513))
    patch(body).value {
      assertThat(it.userMessage).isEqualTo("title: size must be between 1 and 512")
    }
  }

  @Test
  fun `Patch - 400 when target date is formatted incorrectly`() {
    val body = patchRequestBuilder(targetCompletionDate = "not a date")
    patch(body).value {
      assertThat(it.userMessage).isEqualTo("targetCompletionDate: must be a date in format yyyy-MM-dd")
    }
  }

  @Test
  fun `Patch - 400 when status is not one of the allowed values`() {
    patch(patchRequestBuilder(status = "BATMAN")).value {
      assertThat(it.userMessage)
        .isEqualTo("status: must be one of [NOT_STARTED, BLOCKED, DEFERRED, IN_PROGRESS, COMPLETED, ARCHIVED]")
    }
  }

  @Test
  fun `Patch - 400 when created at prison is too long`() {
    patch(patchRequestBuilder(title = "nice", updatedAtPrison = "1".repeat(251)))
      .value { assertThat(it.userMessage).isEqualTo("updatedAtPrison: size must be between 0 and 250") }
  }
}
