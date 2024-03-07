package uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.AuditAction
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.AuditMessage
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.util.UUID

class PlanAuditTests : IntegrationTestBase() {

  @Autowired
  private lateinit var queueService: HmppsQueueService

  @BeforeEach
  fun purgeAuditQueue() {
    val queue = queueService.findByQueueId("audit")

    runBlocking {
      queue?.apply {
        queue.sqsClient.purgeQueue { it.queueUrl(queue.queueUrl) }.await()
      }
    }
  }

  @Test
  fun `Audit is sent on delete plan`() {
    val (crn, planReference) = givenAPlan()

    authedWebTestClient.delete()
      .uri("person/{crn}/plans/{plan}", crn, planReference)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)

    assertAuditMessageSent(auditAction = AuditAction.DELETE_PLAN, planReference)
  }

  @Test
  fun `Audit is sent on create plan`() {
    val (_, planReference) = givenAPlan()

    assertAuditMessageSent(auditAction = AuditAction.CREATE_PLAN, planReference)
  }

  private fun assertAuditMessageSent(auditAction: AuditAction, reference: UUID) {
    val queue = queueService.findByQueueId("audit")!!

    val responseFuture = queue.sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(queue.queueUrl)
        .maxNumberOfMessages(10)
        .build(),
    )
    runBlocking {
      val response = responseFuture.await()
      val sent = response.messages().map { AuditMessage(it.body()) }
      sent.forEach {
        println("${it.what}:${it.correlationId}")
      }

      assertThat(sent).areExactly(
        1,
        Condition(
          { message -> message.what == auditAction.name && message.correlationId == reference.toString() },
          "queue should contain a $auditAction for $reference",
        ),
      )
    }
  }
}
