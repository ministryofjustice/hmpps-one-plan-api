package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateStepRequestTest {
  @Test
  fun `build step entity`() {
    val id = UUID.randomUUID()
    val entity = CreateStepRequest(
      description = "desc",
      status = "status",
      staffNote = "staff note",
      staffTask = true,
    ).buildEntity(id, 1)

    assertThat(entity.objectiveId).isEqualTo(id)
    assertThat(entity.status).isEqualTo("status")
    assertThat(entity.stepOrder).isEqualTo(1)
    assertThat(entity.description).isEqualTo("desc")
    assertThat(entity.staffNote).isEqualTo("staff note")
    assertThat(entity.staffTask).isTrue()
  }
}
