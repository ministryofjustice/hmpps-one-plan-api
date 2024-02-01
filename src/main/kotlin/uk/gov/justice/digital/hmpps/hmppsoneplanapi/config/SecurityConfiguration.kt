package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

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
          .anyExchange().authenticated()
      }
      .oauth2ResourceServer { resourceServer ->
        resourceServer
          .jwt(withDefaults())
      }
    return http.build()
  }
}
