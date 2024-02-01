package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface StepRepository : CoroutineCrudRepository<StepEntity, UUID> {
  suspend fun findByReferenceAndObjectiveId(reference: UUID, objectiveId: UUID): StepEntity?
}
