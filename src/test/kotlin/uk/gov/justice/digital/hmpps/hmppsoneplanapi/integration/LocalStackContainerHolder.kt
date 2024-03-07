package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import org.testcontainers.utility.DockerImageName

object LocalStackContainerHolder {
  private val localstackImage: DockerImageName = DockerImageName.parse("localstack/localstack:3.2.0")

  val instance: LocalStackContainer by lazy {
    LocalStackContainer(localstackImage)
      .withServices(SQS)
      .also { it.start() }
  }
}
