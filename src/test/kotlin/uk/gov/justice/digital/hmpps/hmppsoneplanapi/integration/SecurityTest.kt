package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPattern
import java.util.UUID.randomUUID

class SecurityTest : IntegrationTestBase() {

  @Autowired
  private lateinit var applicationContext: ApplicationContext

  private lateinit var authButMissingRoleClient: WebTestClient

  @BeforeEach
  fun setupClient() {
    if (!::authButMissingRoleClient.isInitialized) {
      val token = jwtAuthHelper.createJwt(subject = "not-authorized", roles = listOf("ANOTHER_ROLE"))

      authButMissingRoleClient = notAuthedWebTestClient
        .mutateWith { builder, _, _ ->
          builder.defaultHeader(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token",
          )
        }
    }
  }

  @TestFactory
  fun `When User is Unauthenticated`(): List<DynamicTest> = allEndpoints().map { (method, path) ->
    dynamicTest("$method -> $path") {
      notAuthedWebTestClient.method(method.asHttpMethod())
        .uri(path.patternString, "123", randomUUID(), randomUUID(), randomUUID())
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .contentLength(0)
    }
  }

  @TestFactory
  fun `When User is does not have ONE_PLAN_EDIT role`(): List<DynamicTest> = allEndpoints().map { (method, path) ->
    dynamicTest("$method -> $path") {
      authButMissingRoleClient.method(method.asHttpMethod())
        .uri(path.patternString, "123", randomUUID(), randomUUID(), randomUUID())
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectHeader()
        .contentLength(0)
    }
  }
  private fun allEndpoints(): List<Pair<RequestMethod, PathPattern>> {
    val requestMappingHandlerMapping =
      applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping::class.java)
    return requestMappingHandlerMapping.handlerMethods.flatMap { (key, _) ->
      key.methodsCondition.methods.flatMap { requestMethod ->
        key.patternsCondition.patterns.map { pattern ->
          requestMethod to pattern
        }
      }
    }.filter { shouldInclude(it.second.patternString) }
  }

  private fun shouldInclude(pattern: String): Boolean = pattern.startsWith("/person")
}
