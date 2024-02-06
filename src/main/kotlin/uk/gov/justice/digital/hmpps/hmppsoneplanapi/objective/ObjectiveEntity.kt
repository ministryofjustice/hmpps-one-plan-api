package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

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
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

@Table(name = "objective")
data class ObjectiveEntity(
  @Id
  @InsertOnlyProperty
  @JsonIgnore
  private val id: UUID = UUID.randomUUID(),
  @InsertOnlyProperty
  val reference: UUID = UUID.randomUUID(),

  val title: String,
  val targetCompletionDate: LocalDate,
  val status: String,
  val note: String,
  val outcome: String,

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
) : Persistable<UUID> {
  @Transient
  private var isNew: Boolean = true

  @JsonIgnore
  override fun getId(): UUID = id

  @JsonIgnore
  override fun isNew(): Boolean = isNew

  fun markAsUpdate(): ObjectiveEntity {
    isNew = false
    return this
  }
}

data class ObjectiveKey(
  val caseReferenceNumber: String,
  val planReference: UUID,
  val objectiveReference: UUID,
)
