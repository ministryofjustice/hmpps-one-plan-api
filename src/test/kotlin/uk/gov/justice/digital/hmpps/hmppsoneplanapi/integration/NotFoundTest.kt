package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.junit.jupiter.api.Test

class NotFoundTest : IntegrationTestBase() {

  @Test
  fun `Resources that aren't found should return 404 - test of the exception handler`() {
    authedWebTestClient.get().uri("/some-url-not-found")
      .exchange()
      .expectStatus().isNotFound
  }
}
