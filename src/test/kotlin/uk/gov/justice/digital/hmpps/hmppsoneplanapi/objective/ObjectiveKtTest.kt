package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class ObjectiveKtTest {
  @Test
  fun `builds and objective from an entity`() {
    val entity = ObjectiveEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      status = ObjectiveStatus.COMPLETED,
      caseReferenceNumber = CaseReferenceNumber("crn"),
      note = "note",
      targetCompletionDate = LocalDate.now(),
      outcome = "outcome",
      title = "title",
      updatedAt = ZonedDateTime.now(),
      createdAt = ZonedDateTime.now().minusDays(1),
      updatedBy = "someone",
      updatedByDisplayName = "Some One",
      createdBy = "someoneElse",
      createdByDisplayName = "Someone Else",
    )

    val result = buildObjective(entity)
    assertThat(result.id).isEqualTo(entity.id)
    assertThat(result.reference).isEqualTo(entity.reference)
    assertThat(result.status).isEqualTo(entity.status)
    assertThat(result.caseReferenceNumber).isEqualTo(entity.caseReferenceNumber)
    assertThat(result.note).isEqualTo(entity.note)
    assertThat(result.targetCompletionDate).isEqualTo(entity.targetCompletionDate)
    assertThat(result.title).isEqualTo(entity.title)
    assertThat(result.outcome).isEqualTo(entity.outcome)
    assertThat(result.updatedAt).isEqualTo(entity.updatedAt)
    assertThat(result.updatedBy).isEqualTo(entity.updatedBy)
    assertThat(result.createdAt).isEqualTo(entity.createdAt)
    assertThat(result.createdBy).isEqualTo(entity.createdBy)
    assertThat(result.createdByDisplayName).isEqualTo(entity.createdByDisplayName)
    assertThat(result.updatedByDisplayName).isEqualTo(entity.updatedByDisplayName)
    assertThat(result.steps).isNull()
  }

  @Test
  fun `has all of the fields of an entity`() {
    assertThat(allProps(Objective::class))
      .containsAll(allProps(ObjectiveEntity::class) - "isNew")
  }

  private fun allProps(type: KClass<*>) = type.declaredMemberProperties.map { it.name }
}
