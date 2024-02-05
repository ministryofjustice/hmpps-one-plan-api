package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.ZonedDateTime
import java.util.UUID

@Table(name = "objective_history")
data class ObjectiveHistory(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  val id: UUID = UUID.randomUUID(),
  val objectiveId: UUID,

  val previousValue: String,
  val newValue: String,
  val reasonForChange: String,
  val updatedBy: String,
  val updatedAt: ZonedDateTime,
)
