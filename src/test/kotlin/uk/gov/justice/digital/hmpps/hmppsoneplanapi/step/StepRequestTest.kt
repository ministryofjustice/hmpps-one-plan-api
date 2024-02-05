package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class StepRequestTest {
  @Test
  fun `build step entity`() {
    val id = UUID.randomUUID()
    val entity = StepRequest(
      "desc",
      1,
      "status",
    ).buildEntity(id)

    assertThat(entity.objectiveId).isEqualTo(id)
    assertThat(entity.status).isEqualTo("status")
    assertThat(entity.stepOrder).isEqualTo(1)
    assertThat(entity.description).isEqualTo("desc")
  }
}
