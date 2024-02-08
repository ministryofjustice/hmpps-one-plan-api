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

  // <editor-fold desc="POST">
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

  @Test
  fun `Post - 400 when staffNote is too long`() {
    val body = createRequestBuilder(staffNote = "C".repeat(513))
    post(body).value {
      assertThat(it.userMessage).isEqualTo("staffNote: size must be between 0 and 512")
    }
  }
  // </editor-fold>

  private fun createRequestBuilder(
    description: Any? = "description",
    stepOrder: Any? = "1",
    status: Any? = "status",
    staffTask: Any? = "false",
    staffNote: Any? = null,
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "description" to description,
          "status" to status,
          "stepOrder" to stepOrder,
          "staffTask" to staffTask,
          "staffNote" to staffNote,
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

  // <editor-fold desc="PUT">
  @Test
  fun `Put - 400 when description field is too long`() {
    val body = updateRequestBuilder(description = "B".repeat(513))
    put(body).value {
      assertThat(it.userMessage).isEqualTo("description: size must be between 1 and 512")
    }
  }

  @Test
  fun `Put - 400 when description field is null`() {
    val body = updateRequestBuilder(description = null)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("description: is required")
    }
  }

  @Test
  fun `Put - 400 when description field is blank`() {
    val body = updateRequestBuilder(description = "\n   ")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("description: must not be blank")
    }
  }

  @Test
  fun `Put - 400 when status field is too long`() {
    val body = updateRequestBuilder(status = "B".repeat(51))
    put(body).value {
      assertThat(it.userMessage).isEqualTo("status: size must be between 1 and 50")
    }
  }

  @Test
  fun `Put - 400 when status field is null`() {
    val body = updateRequestBuilder(status = null)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("status: is required")
    }
  }

  @Test
  fun `Put - 400 when status field is blank`() {
    val body = updateRequestBuilder(status = "\n   ")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("status: must not be blank")
    }
  }

  @Test
  fun `Put - 400 when staffTask is not present`() {
    val body = updateRequestBuilder(staffTask = null)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: is required")
    }
  }

  @Test
  fun `Put - 400 when staffTask is not a boolean string`() {
    val body = updateRequestBuilder(staffTask = "gunk")
    put(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: should be a boolean true|false")
    }
  }

  @Test
  fun `Put - 400 when staffTask is not a boolean`() {
    val body = updateRequestBuilder(staffTask = 4.1)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: should be a boolean true|false")
    }
  }

  @Test
  fun `Put - 400 when staffNote is too long`() {
    val body = updateRequestBuilder(staffNote = "C".repeat(513))
    put(body).value {
      assertThat(it.userMessage).isEqualTo("staffNote: size must be between 0 and 512")
    }
  }

  @Test
  fun `Put - 400 when reasonForChange field is too long`() {
    val body = updateRequestBuilder(reasonForChange = "C".repeat(251))
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
  // </editor-fold>

  private fun updateRequestBuilder(
    description: Any? = "description",
    stepOrder: Any? = "1",
    status: Any? = "status",
    staffTask: Any? = "false",
    staffNote: Any? = null,
    reasonForChange: Any? = "reason for change",
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "description" to description,
          "status" to status,
          "stepOrder" to stepOrder,
          "staffTask" to staffTask,
          "staffNote" to staffNote,
          "reasonForChange" to reasonForChange,
        ).filter { it.value != null },
      )
  }

  private fun put(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.put()
      .uri(
        "/person/123/plans/{ref}/objectives/{oRef}/steps/{sRef}",
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)
}
