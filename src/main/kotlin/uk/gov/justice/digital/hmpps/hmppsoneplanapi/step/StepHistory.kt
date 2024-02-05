package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.ZonedDateTime
import java.util.UUID

@Table("step_history")
data class StepHistory(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  val id: UUID = UUID.randomUUID(),
  val stepId: UUID,

  val previousValue: String,
  val newValue: String,
  val reasonForChange: String,
  val updatedBy: String,
  val updatedAt: ZonedDateTime,
)
