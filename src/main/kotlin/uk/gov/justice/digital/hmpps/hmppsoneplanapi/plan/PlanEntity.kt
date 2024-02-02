package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.ZonedDateTime
import java.util.UUID

@Table(name = "plan")
data class PlanEntity(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  private val id: UUID = UUID.randomUUID(),
  @InsertOnlyProperty
  val reference: UUID = UUID.randomUUID(),
  @InsertOnlyProperty
  val prisonNumber: String,
  val type: PlanType,

  @InsertOnlyProperty
  val createdBy: String,
  @InsertOnlyProperty
  @CreatedDate
  val createdAt: ZonedDateTime? = null,
  var updatedBy: String = createdBy,
  @LastModifiedDate
  var updatedAt: ZonedDateTime? = createdAt,

  @JsonIgnore
  var isDeleted: Boolean = false,
) : Persistable<UUID> {
  override fun getId(): UUID = id

  @JsonIgnore
  override fun isNew(): Boolean = !isDeleted
}

enum class PlanType {
  PERSONAL_LEARNING,
  SENTENCE,
  RESETTLEMENT,
}

data class PlanKey(val prisonNumber: String, val reference: UUID)
