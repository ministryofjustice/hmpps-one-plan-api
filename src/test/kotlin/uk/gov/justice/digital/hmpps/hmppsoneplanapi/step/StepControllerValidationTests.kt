package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.ninjasquad.springmockk.MockkBean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.WebfluxTestBase
import java.util.UUID

@WebFluxTest(controllers = [StepController::class])
class StepControllerValidationTests : WebfluxTestBase() {

  @MockkBean
  private lateinit var stepService: StepService

  @Test
  fun `Post - 400 when description field is too long`() {
    val body = createRequestBuilder(description = "B".repeat(513))
    post(body).value {
      assertThat(it.userMessage).isEqualTo("description: size must be between 1 and 512")
    }
  }

  @Test
  fun `Post - 400 when description field is null`() {
    val body = createRequestBuilder(description = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("description: is required")
    }
  }

  @Test
  fun `Post - 400 when description field is blank`() {
    val body = createRequestBuilder(description = "\n   ")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("description: must not be blank")
    }
  }

  @Test
  fun `Post - 400 when status field is too long`() {
    val body = createRequestBuilder(status = "B".repeat(51))
    post(body).value {
      assertThat(it.userMessage).isEqualTo("status: size must be between 1 and 50")
    }
  }

  @Test
  fun `Post - 400 when status field is null`() {
    val body = createRequestBuilder(status = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("status: is required")
    }
  }

  @Test
  fun `Post - 400 when status field is blank`() {
    val body = createRequestBuilder(status = "\n   ")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("status: must not be blank")
    }
  }

  @Test
  fun `Post - 400 when staffTask is not present`() {
    val body = createRequestBuilder(staffTask = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: is required")
    }
  }

  @Test
  fun `Post - 400 when staffTask is not a boolean string`() {
    val body = createRequestBuilder(staffTask = "gunk")
    post(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: should be a boolean true|false")
    }
  }

  @Test
  fun `Post - 400 when staffTask is not a boolean`() {
    val body = createRequestBuilder(staffTask = 4.1)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: should be a boolean true|false")
    }
  }

  private fun createRequestBuilder(
    description: Any? = "description",
    stepOrder: Any? = "1",
    status: Any? = "status",
    staffTask: Any? = "false",
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "description" to description,
          "status" to status,
          "stepOrder" to stepOrder,
          "staffTask" to staffTask,
        ).filter { it.value != null },
      )
  }

  private fun post(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/123/plans/{ref}/objectives/{oRef}/steps", UUID.randomUUID(), UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)
}
