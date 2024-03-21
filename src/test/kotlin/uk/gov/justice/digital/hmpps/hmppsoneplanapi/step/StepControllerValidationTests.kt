package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
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
  fun `Post - 400 when status field is null`() {
    val body = createRequestBuilder(status = null)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("status: is required")
    }
  }

  @Test
  fun `Post - 400 when status field is not one of allowed values`() {
    val body = createRequestBuilder(status = "MOOSE")
    post(body).value {
      assertThat(it.userMessage)
        .isEqualTo("status: must be one of [NOT_STARTED, BLOCKED, DEFERRED, IN_PROGRESS, COMPLETED, ARCHIVED]")
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
      assertThat(it.userMessage).isEqualTo("staffTask: must be a boolean true|false")
    }
  }

  @Test
  fun `Post - 400 when staffTask is not a boolean`() {
    val body = createRequestBuilder(staffTask = 4.1)
    post(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: must be a boolean true|false")
    }
  }

  @Test
  fun `Post - 400 when staffNote is too long`() {
    val body = createRequestBuilder(staffNote = "C".repeat(513))
    post(body).value {
      assertThat(it.userMessage).isEqualTo("staffNote: size must be between 0 and 512")
    }
  }

  @Test
  fun `Post - 400 when created at prison is too long`() {
    post(createRequestBuilder(createdAtPrison = "1".repeat(251)))
      .value { assertThat(it.userMessage).isEqualTo("createdAtPrison: size must be between 0 and 250") }
  }
  // </editor-fold>

  private fun createRequestBuilder(
    description: Any? = "description",
    stepOrder: Any? = "1",
    status: Any? = "IN_PROGRESS",
    staffTask: Any? = "false",
    staffNote: Any? = null,
    createdAtPrison: Any? = null,
  ): String {
    return objectMapper
      .writeValueAsString(
        mapOf(
          "description" to description,
          "status" to status,
          "stepOrder" to stepOrder,
          "staffTask" to staffTask,
          "staffNote" to staffNote,
          "createdAtPrison" to createdAtPrison,
        ).filter { it.value != null },
      )
  }

  private fun post(body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.post()
      .uri("/person/123/objectives/{oRef}/steps", UUID.randomUUID())
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
  fun `Put - 400 when status field is null`() {
    val body = updateRequestBuilder(status = null)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("status: is required")
    }
  }

  @Test
  fun `Put - 400 when status field is not one of allowed values`() {
    val body = updateRequestBuilder(status = "EGG")
    put(body).value {
      assertThat(it.userMessage)
        .isEqualTo("status: must be one of [NOT_STARTED, BLOCKED, DEFERRED, IN_PROGRESS, COMPLETED, ARCHIVED]")
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
      assertThat(it.userMessage).isEqualTo("staffTask: must be a boolean true|false")
    }
  }

  @Test
  fun `Put - 400 when staffTask is not a boolean`() {
    val body = updateRequestBuilder(staffTask = 4.1)
    put(body).value {
      assertThat(it.userMessage).isEqualTo("staffTask: must be a boolean true|false")
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

  @Test
  fun `Put - 400 when created at prison is too long`() {
    put(updateRequestBuilder(description = "newDesc", updatedAtPrison = "1".repeat(251)))
      .value { assertThat(it.userMessage).isEqualTo("updatedAtPrison: size must be between 0 and 250") }
  }
  // </editor-fold>

  private fun updateRequestBuilder(
    description: Any? = "description",
    stepOrder: Any? = "1",
    status: Any? = "COMPLETED",
    staffTask: Any? = "false",
    staffNote: Any? = null,
    reasonForChange: Any? = "reason for change",
    updatedAtPrison: Any? = null,
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
          "updatedAtPrison" to updatedAtPrison,
        ).filter { it.value != null },
      )
  }

  private fun put(body: String): WebTestClient.BodySpec<ErrorResponse, *> = request(HttpMethod.PUT, body)

  private fun request(method: HttpMethod, body: String): WebTestClient.BodySpec<ErrorResponse, *> =
    authedWebTestClient.method(method)
      .uri(
        "/person/123/objectives/{oRef}/steps/{sRef}",
        UUID.randomUUID(),
        UUID.randomUUID(),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody(ErrorResponse::class.java)

  @Test
  fun `422 when a unique constraint violation happens`() {
    coEvery { stepService.createStep(any(), any()) }.throws(DuplicateKeyException("No!"))

    authedWebTestClient.post()
      .uri("/person/123/objectives/{oRef}/steps", UUID.randomUUID(), UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(createRequestBuilder())
      .exchange()
      .expectStatus()
      .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
      .expectBody(ErrorResponse::class.java)
      .value { assertThat(it.userMessage).isEqualTo("unexpected error, please retry") }
  }

  @Test
  fun `Patch 400 on patch when description is too long`() {
    request(HttpMethod.PATCH, patchRequestBuilder(description = "Z".repeat(513)))
      .value {
        assertThat(it.userMessage).isEqualTo("description: size must be between 1 and 512")
      }
  }

  @Test
  fun `Patch 400 on patch when note is too long`() {
    request(HttpMethod.PATCH, patchRequestBuilder(staffNote = "C".repeat(513)))
      .value {
        assertThat(it.userMessage).isEqualTo("staffNote: size must be between 0 and 512")
      }
  }

  @Test
  fun `Patch 400 on patch when reason is too long`() {
    request(HttpMethod.PATCH, patchRequestBuilder(reasonForChange = "C".repeat(251)))
      .value {
        assertThat(it.userMessage).isEqualTo("reasonForChange: size must be between 1 and 250")
      }
  }

  @Test
  fun `Patch 400 on patch when reason is missing`() {
    request(HttpMethod.PATCH, patchRequestBuilder(reasonForChange = null))
      .value {
        assertThat(it.userMessage).isEqualTo("reasonForChange: is required")
      }
  }

  @Test
  fun `Patch - 400 when created at prison is too long`() {
    request(HttpMethod.PATCH, patchRequestBuilder(description = "newDesc", updatedAtPrison = "1".repeat(251)))
      .value { assertThat(it.userMessage).isEqualTo("updatedAtPrison: size must be between 0 and 250") }
  }

  private fun patchRequestBuilder(
    description: Any? = null,
    stepOrder: Any? = null,
    status: Any? = null,
    staffTask: Any? = null,
    staffNote: Any? = null,
    reasonForChange: Any? = "reason for change",
    updatedAtPrison: Any? = null,
  ): String = updateRequestBuilder(
    description = description,
    stepOrder = stepOrder,
    staffNote = staffNote,
    staffTask = staffTask,
    status = status,
    reasonForChange = reasonForChange,
    updatedAtPrison = updatedAtPrison,
  )
}
