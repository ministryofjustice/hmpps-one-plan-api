package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.awaitility.Awaitility.await
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.AuditAction
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Duration
import java.util.UUID

class AuditTestBase : IntegrationTestBase() {

  @Autowired
  private lateinit var queueService: HmppsQueueService

  fun purgeAuditQueue() {
    val queue = queueService.findByQueueId("audit")

    runBlocking {
      queue?.apply {
        queue.sqsClient.purgeQueue { it.queueUrl(queue.queueUrl) }.await()
      }
    }
  }

  fun assertAuditMessageSent(auditAction: AuditAction, reference: UUID) {
    val queue = queueService.findByQueueId("audit")!!

    val responseFuture = queue.sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(queue.queueUrl)
        .maxNumberOfMessages(10)
        .build(),
    )

    // Retry as call to audit services is done in a background future and is not guaranteed to have completed
    // after the api call completes
    await().atMost(Duration.ofSeconds(2))
      .pollDelay(Duration.ZERO)
      .untilAsserted {
        val sent = runBlocking {
          val response = responseFuture.await()
          response.messages().map { AuditMessage(it.body()) }
        }

        assertThat(sent).areExactly(
          1,
          Condition(
            { message -> message.what == auditAction.name && message.correlationId == reference.toString() },
            "audit queue should contain a $auditAction for $reference",
          ),
        )
      }
  }
}
