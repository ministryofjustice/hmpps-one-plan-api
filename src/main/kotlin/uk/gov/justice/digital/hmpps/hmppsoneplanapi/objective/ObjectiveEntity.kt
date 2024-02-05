package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

@Table(name = "objective")
data class ObjectiveEntity(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  val id: UUID = UUID.randomUUID(),
  @InsertOnlyProperty
  val reference: UUID = UUID.randomUUID(),

  val title: String,
  val targetCompletionDate: LocalDate,
  val status: String,
  val note: String,
  val outcome: String,

  @InsertOnlyProperty
  val createdBy: String = "TODO",
  @InsertOnlyProperty
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val updatedBy: String = createdBy,
  val updatedAt: ZonedDateTime = createdAt,
)

data class ObjectiveKey(
  val prisonNumber: String,
  val planReference: UUID,
  val objectiveReference: UUID,
)
