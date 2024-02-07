package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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

  @Nested
  @DisplayName("Post")
  inner class PostTests {
    @Test
    fun `400 when title is too long`() {
      val aLotOfAs = "a".repeat(1000)
      val body = createRequestBuilder(title = aLotOfAs)
      post(body).value {
        assertThat(it.userMessage).isEqualTo("title: size must be between 1 and 512")
      }
    }

    @Test
    fun `400 when title is blank`() {
      val body = createRequestBuilder(title = "      \n")
      post(body).value {
        assertThat(it.userMessage).isEqualTo("title: must not be blank")
      }
    }

    @Test
    fun `400 when title is null`() {
      val body = createRequestBuilder(title = null)
      post(body).value {
        assertThat(it.userMessage).isEqualTo("title: is required")
      }
    }

    @Test
    fun `400 when status is null`() {
      val body = createRequestBuilder(status = null)
      post(body).value {
        assertThat(it.userMessage).isEqualTo("status: is required")
      }
    }

    @Test
    fun `400 when status is too long`() {
      val aLotOfAs = "a".repeat(51)
      val body = createRequestBuilder(status = aLotOfAs)
      post(body).value {
        assertThat(it.userMessage).isEqualTo("status: size must be between 1 and 50")
      }
    }

    @Test
    fun `400 when status is blank`() {
      val body = createRequestBuilder(status = "      \n")
      post(body).value {
        assertThat(it.userMessage).isEqualTo("status: must not be blank")
      }
    }

    @Test
    fun `400 when target date is formatted incorrectly`() {
      val body = createRequestBuilder(targetCompletionDate = "not a date")
      post(body).value {
        assertThat(it.userMessage).isEqualTo("targetCompletionDate: should be a date in format yyyy-MM-dd")
      }
    }

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

    @Nested
    @DisplayName("Put")
    inner class PutTests {
      @Test
      fun `400 when title is too long`() {
        val aLotOfAs = "a".repeat(1000)
        val body = updateRequestBuilder(title = aLotOfAs)
        put(body).value {
          assertThat(it.userMessage).isEqualTo("title: size must be between 1 and 512")
        }
      }

      @Test
      fun `400 when title is blank`() {
        val body = updateRequestBuilder(title = "      \n")
        put(body).value {
          assertThat(it.userMessage).isEqualTo("title: must not be blank")
        }
      }

      @Test
      fun `400 when title is null`() {
        val body = updateRequestBuilder(title = null)
        put(body).value {
          assertThat(it.userMessage).isEqualTo("title: is required")
        }
      }

      @Test
      fun `400 when status is null`() {
        val body = updateRequestBuilder(status = null)
        put(body).value {
          assertThat(it.userMessage).isEqualTo("status: is required")
        }
      }

      @Test
      fun `400 when status is too long`() {
        val aLotOfAs = "a".repeat(51)
        val body = updateRequestBuilder(status = aLotOfAs)
        put(body).value {
          assertThat(it.userMessage).isEqualTo("status: size must be between 1 and 50")
        }
      }

      @Test
      fun `400 when status is blank`() {
        val body = updateRequestBuilder(status = "      \n")
        put(body).value {
          assertThat(it.userMessage).isEqualTo("status: must not be blank")
        }
      }

      @Test
      fun `400 when target date is formatted incorrectly`() {
        val body = updateRequestBuilder(targetCompletionDate = "not a date")
        put(body).value {
          assertThat(it.userMessage).isEqualTo("targetCompletionDate: should be a date in format yyyy-MM-dd")
        }
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
  }
}
