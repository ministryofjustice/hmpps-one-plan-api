package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.DisplayNameAudited
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
  val status: StepStatus,
  val staffNote: String?,
  val staffTask: Boolean,

  @InsertOnlyProperty
  @CreatedBy
  val createdBy: String? = null,
  @InsertOnlyProperty
  @CreatedDate
  val createdAt: ZonedDateTime? = null,
  @LastModifiedBy
  val updatedBy: String? = createdBy,
  @LastModifiedDate
  val updatedAt: ZonedDateTime? = createdAt,
  override var createdByDisplayName: String? = null,
  override var updatedByDisplayName: String? = null,
  val createdAtPrison: String? = null,
  val updatedAtPrison: String? = createdAtPrison,
  @JsonIgnore
  val isDeleted: Boolean = false,
) : Persistable<UUID>, DisplayNameAudited {
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

enum class StepStatus {
  NOT_STARTED,
  BLOCKED,
  DEFERRED,
  IN_PROGRESS,
  COMPLETED,
  ARCHIVED,
}
