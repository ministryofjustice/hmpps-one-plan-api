package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

object PostgresContainer {
  val instance: PostgreSQLContainer<Nothing> by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing> {
    log.info("Creating a Postgres database")
    return PostgreSQLContainer<Nothing>("postgres:16.1").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withDatabaseName("one-plan")
      withUsername("one-plan")
      withPassword("one-plan")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)

      start()
    }
  }

  private val log = LoggerFactory.getLogger(this::class.java)
}
