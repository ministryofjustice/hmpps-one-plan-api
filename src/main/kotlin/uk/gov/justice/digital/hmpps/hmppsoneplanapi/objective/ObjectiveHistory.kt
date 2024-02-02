package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
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

  val previousValue: JsonNode,
  val newValue: JsonNode,
  val reasonForChange: String,
  val updatedBy: String,
  val updatedAt: ZonedDateTime,
)
