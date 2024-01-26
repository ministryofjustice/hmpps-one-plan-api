package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

class FlywayTest: IntegrationTestBase() {
  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate
  @Test
  fun `runs db init scripts`() {
    assertThat(jdbcTemplate.queryForObject<Int>("select count(*) from flyway_schema_history"))
      .isGreaterThanOrEqualTo(1)

    assertThat(jdbcTemplate.queryForList("SELECT tablename FROM pg_tables where schemaname = 'public'", String::class.java))
      .contains("plan")
  }
}