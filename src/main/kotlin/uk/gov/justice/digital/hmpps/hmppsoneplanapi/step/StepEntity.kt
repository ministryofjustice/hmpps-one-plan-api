package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.ZonedDateTime
import java.util.UUID

@Table("step")
data class StepEntity(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  val id: UUID = UUID.randomUUID(),
  @InsertOnlyProperty
  val reference: UUID = UUID.randomUUID(),
  val objectiveId: UUID,

  val description: String,
  val stepOrder: Int,
  val status: String,

  @InsertOnlyProperty
  val createdBy: String = "TODO",
  @InsertOnlyProperty
  val createdAt: ZonedDateTime = ZonedDateTime.now(),
  val updatedBy: String = createdBy,
  val updatedAt: ZonedDateTime = createdAt,
)
