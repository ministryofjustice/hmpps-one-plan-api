package uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions

import java.util.UUID
import kotlin.reflect.KClass

class UpdateNotAllowedException(type: KClass<*>, reference: UUID) :
  Exception("Cannot update ${type.simpleName}:$reference")
