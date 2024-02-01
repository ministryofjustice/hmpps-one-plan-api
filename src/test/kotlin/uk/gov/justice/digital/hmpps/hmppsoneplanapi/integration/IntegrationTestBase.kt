package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.common.CreateEntityResponse
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.CreatePlanRequest
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanKey
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.plan.PlanType

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient
  lateinit var authedWebTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthTestOverride

  @BeforeEach
  fun setupAuth() {
    if (!::authedWebTestClient.isInitialized) {
      authedWebTestClient = webTestClient
        .mutateWith { builder, _, _ -> builder.defaultHeader(HttpHeaders.AUTHORIZATION, jwtAuthHelper.createAuthHeader()) }
    }
  }

  companion object {
    private val pgContainer = PostgresContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer.run {
        registry.add("spring.flyway.url", pgContainer::getJdbcUrl)
        registry.add("spring.flyway.user", pgContainer::getUsername)
        registry.add("spring.flyway.password", pgContainer::getPassword)
        registry.add("spring.r2dbc.url") { "r2dbc:postgresql://${pgContainer.host}:${pgContainer.getMappedPort(5432)}/${pgContainer.databaseName}?sslMode=disable" }
        registry.add("spring.r2dbc.username", pgContainer::getUsername)
        registry.add("spring.r2dbc.password", pgContainer::getPassword)
      }
    }
  }

  fun givenAPlan(prisonNumber: String = "123", type: PlanType = PlanType.PERSONAL_LEARNING): PlanKey {
    val reference = authedWebTestClient.post().uri("/person/{prisonNumber}/plans", prisonNumber)
      .bodyValue(CreatePlanRequest(planType = type))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(CreateEntityResponse::class.java)
      .returnResult()
      .responseBody!!
      .reference
    return PlanKey(prisonNumber, reference)
  }

  fun givenPlanIsDeleted(planKey: PlanKey) {
    authedWebTestClient.delete()
      .uri("/person/{number}/plans/{ref}", planKey.prisonNumber, planKey.reference)
      .exchange()
      .expectStatus()
      .isNoContent()
  }
}
