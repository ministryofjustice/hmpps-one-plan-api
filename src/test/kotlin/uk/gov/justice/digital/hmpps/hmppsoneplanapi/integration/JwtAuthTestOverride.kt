package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

@Component
class JwtAuthTestOverride {
  private val keyPair: KeyPair = createKeyPair()

  private fun createKeyPair(): KeyPair {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    return gen.generateKeyPair()
  }

  @Bean
  fun jwtDecoder(): ReactiveJwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()
  fun createAuthHeader(): String {
    val token = createJwt(
      subject = "test-user",
      scope = listOf(),
      expiryTime = Duration.ofHours(1L),
      roles = listOf("ROLE_ONE_PLAN_EDIT"),
      displayName = "Test User",
    )
    return "Bearer $token"
  }

  fun createJwt(
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
    displayName: String? = null,
  ): String = mutableMapOf<String, Any>()
    .also { subject?.let { subject -> it["user_name"] = subject } }
    .also { roles?.let { roles -> it["authorities"] = roles } }
    .also { scope?.let { scope -> it["scope"] = scope } }
    .also { displayName?.let { displayName -> it["name"] = displayName } }
    .let {
      Jwts.builder()
        .id(jwtId)
        .subject(subject)
        .claims(it.toMap())
        .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
        .signWith(keyPair.private)
        .compact()
    }
}
