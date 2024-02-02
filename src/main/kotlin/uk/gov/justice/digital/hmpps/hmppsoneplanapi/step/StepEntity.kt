package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.ZonedDateTime
import java.util.UUID

@Table("step")
data class StepEntity(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  private val id: UUID = UUID.randomUUID(),
  @InsertOnlyProperty
  val reference: UUID = UUID.randomUUID(),
  val objectiveId: UUID,

  val description: String,
  val stepOrder: Int,
  val status: String,

  @InsertOnlyProperty
  val createdBy: String = "TODO",
  @InsertOnlyProperty
  @CreatedDate
  val createdAt: ZonedDateTime? = null,
  val updatedBy: String = createdBy,
  @LastModifiedDate
  val updatedAt: ZonedDateTime? = createdAt,
  @JsonIgnore
  val isDeleted: Boolean = false,
) : Persistable<UUID> {
  @Transient
  private var isNew = true

  @JsonIgnore
  override fun getId(): UUID = id

  @JsonIgnore
  override fun isNew() = isNew

  fun markAsUpdate(): StepEntity {
    isNew = false
    return this
  }
}
