package uk.gov.justice.digital.hmpps.hmppsoneplanapi.step

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CaseReferenceNumber
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.CreateObjectiveRequest
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveService
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveStatus
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.objective.ObjectiveType
import java.time.Duration
import java.util.UUID

class StepServiceTest : IntegrationTestBase() {
  @Autowired
  private lateinit var stepService: StepService

  @Autowired
  private lateinit var transactionHelper: StepTransactionHelper

  @Autowired
  private lateinit var objectiveService: ObjectiveService

  @Autowired
  private lateinit var databaseClient: DatabaseClient

  @Test
  @WithMockUser
  fun `Check updates are sequenced`() {
    runBlocking {
      val objectiveKey = givenAnObjective()
      val stepReference = givenAStep(objectiveKey)
      val channel = Channel<Any>()
      launch {
        transactionHelper.updateAndWait(channel, objectiveKey, stepReference, anUpdate("1"))
      }

      delay(Duration.ofMillis(5))
      transactionHelper.updateStep(objectiveKey, stepReference, anUpdate("2"))

      val updatedStep = stepService.getStep(objectiveKey, stepReference)
      assertThat(updatedStep.description).isEqualTo("2")

      assertThat(countOfHistoryRecords(stepReference)).isEqualTo(2)
    }
  }

  private suspend fun countOfHistoryRecords(reference: UUID): Long = databaseClient.sql(
    """ select count(*) from step_history where step_id =
        | (select id from step where reference = :reference)
    """.trimMargin(),
  )
    .bind("reference", reference)
    .mapValue(Long::class.javaObjectType)
    .awaitSingle()

  private fun anUpdate(description: String) = PutStepRequest(
    description = description,
    status = StepStatus.IN_PROGRESS,
    reasonForChange = "for this test",
    staffTask = false,
    staffNote = null,
  )

  suspend fun givenAnObjective(): ObjectiveKey {
    val crn = CaseReferenceNumber("1007")
    val objective = objectiveService.createObjective(
      crn,
      CreateObjectiveRequest(
        title = "title",
        status = ObjectiveStatus.IN_PROGRESS,
        type = ObjectiveType.HEALTH,
      ),
    )
    return ObjectiveKey(crn, objective.reference)
  }

  suspend fun givenAStep(objectiveKey: ObjectiveKey): UUID {
    val step = stepService.createStep(
      objectiveKey,
      CreateStepRequest(
        description = "description",
        staffNote = "note",
        staffTask = false,
        status = StepStatus.NOT_STARTED,
      ),
    )
    return step.reference
  }
}

@Component
internal class StepTransactionHelper(private val service: StepService) {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  suspend fun updateStep(objectiveKey: ObjectiveKey, stepReference: UUID, request: PutStepRequest) = service.updateStep(objectiveKey, stepReference, request)

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  suspend fun updateAndWait(
    channel: Channel<Any>,
    objectiveKey: ObjectiveKey,
    stepReference: UUID,
    request: PutStepRequest,
  ) {
    service.updateStep(objectiveKey, stepReference, request)
    // Hold lock until other transaction has started
    delay(Duration.ofMillis(20))
  }
}
