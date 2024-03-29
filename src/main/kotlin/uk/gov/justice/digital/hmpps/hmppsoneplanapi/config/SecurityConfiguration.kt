package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http
      .csrf { it.disable() }
      .authorizeExchange { authorize ->
        authorize
          .pathMatchers(
            "/webjars/**",
            "/favicon.ico",
            "/health/**",
            "/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
          )
          .permitAll()
          .anyExchange().hasRole("ONE_PLAN_EDIT")
      }
      .oauth2ResourceServer { resourceServer ->
        resourceServer
          .jwt { customizer ->
            customizer.jwtAuthenticationConverter(grantedAuthoritiesExtractor())
          }
      }
    return http.build()
  }
}

private fun grantedAuthoritiesExtractor(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  val jwtAuthenticationConverter = JwtAuthenticationConverter()
  jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesExtractor())
  return ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter)
}

internal class GrantedAuthoritiesExtractor : Converter<Jwt, Collection<GrantedAuthority>> {
  override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
    val authorities: Any = jwt.claims
      .getOrDefault("authorities", emptyList<String>())

    val parsedAuthorities = when (authorities) {
      is List<*> -> castToListOfStrings(authorities)
      is String -> authorities.split(",")
      else -> {
        logger.warn { "Unexpected authorities $authorities" }
        listOf()
      }
    }

    return parsedAuthorities
      .map { SimpleGrantedAuthority(it) }
  }

  @Suppress("UNCHECKED_CAST")
  private fun castToListOfStrings(authorities: List<*>) = authorities as List<String>
}
