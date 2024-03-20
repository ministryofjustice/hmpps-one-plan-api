package uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective

import io.r2dbc.spi.Row
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.step.StepEntity

data class ObjectiveAndStep(
  val objective: ObjectiveEntity,
  val step: StepEntity?,
)

@ReadingConverter
class StepAndObjectiveConverter : Converter<Row, ObjectiveAndStep> {
  override fun convert(source: Row): ObjectiveAndStep {
    val step = mapStep(source)
    val objective = mapObjective(source)
    return ObjectiveAndStep(objective, step)
  }

  private fun mapStep(source: Row) = if (source.get("step_id") == null) {
    null
  } else {
    StepEntity(
      id = source.getMandatory("step_id"),
      reference = source.getMandatory("step_reference"),
      objectiveId = source.getMandatory("objective_id"),
      description = source.getMandatory("step_description"),
      stepOrder = source.getMandatory("step_order", Int::class.javaObjectType),
      staffNote = source.get("staff_note", String::class.java),
      staffTask = source.getMandatory("staff_task", Boolean::class.javaObjectType),
      status = source.getMandatoryEnum("step_status"),
      createdAt = source.getOptional("step_created_at"),
      createdBy = source.getOptional("step_created_by"),
      createdByDisplayName = source.getOptional("step_created_by_display_name"),
      updatedAt = source.getOptional("step_updated_at"),
      updatedBy = source.getOptional("step_updated_by"),
      updatedByDisplayName = source.getOptional("step_updated_by_display_name"),
    )
  }

  private fun mapObjective(source: Row) = ObjectiveEntity(
    id = source.getMandatory("objective_id"),
    reference = source.getMandatory("objective_reference"),
    caseReferenceNumber = CaseReferenceNumber(source.getMandatory("crn")),
    title = source.getMandatory("objective_title"),
    status = source.getMandatoryEnum("objective_status"),
    note = source.get("note", String::class.java),
    outcome = source.get("outcome", String::class.java),
    targetCompletionDate = source.getOptional("target_completion_date"),
    createdAt = source.getOptional("objective_created_at"),
    createdBy = source.getOptional("objective_created_by"),
    createdByDisplayName = source.getOptional("objective_created_by_display_name"),
    updatedAt = source.getOptional("objective_updated_at"),
    updatedBy = source.getOptional("objective_updated_by"),
    updatedByDisplayName = source.getOptional("objective_updated_by_display_name"),
  )
}

private inline fun <reified T : Any> Row.getMandatory(column: String): T = getMandatory(column, T::class.java)
private inline fun <reified T : Any> Row.getOptional(column: String): T? = get(column, T::class.java)
private fun <T : Any> Row.getMandatory(column: String, type: Class<T>): T =
  this.get(column, type) ?: throw NullPointerException("Null value for non-null column $column")

private inline fun <reified T : Enum<T>> Row.getMandatoryEnum(column: String): T =
  enumValueOf<T>(getMandatory(column, String::class.java))
