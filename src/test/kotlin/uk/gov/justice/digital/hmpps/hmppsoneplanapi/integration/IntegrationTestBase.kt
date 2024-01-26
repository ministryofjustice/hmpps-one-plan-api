package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  companion object {
    private val pgContainer = PostgresContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.run {
        System.getenv("5123")
        registry.add("DATABASE_NAME", pgContainer::getDatabaseName)
        registry.add("DATABASE_ENDPOINT") { "localhost:${pgContainer.getMappedPort(5432)}" }
        registry.add("DATABASE_PASSWORD", pgContainer::getPassword)
        registry.add("DATABASE_USERNAME", pgContainer::getUsername)
      }
    }
  }
}
