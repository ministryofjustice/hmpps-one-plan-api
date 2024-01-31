package uk.gov.justice.digital.hmpps.hmppsoneplanapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient

class FlywayTest : IntegrationTestBase() {
  @Autowired
  private lateinit var client: DatabaseClient

  @Test
  fun `runs db init scripts`() {
    val result = client.sql("select count(*) from flyway_schema_history").fetch().one().block()!!
    assertThat(result["count"] as Long).isGreaterThanOrEqualTo(1)

    val tables =
      client.sql("SELECT tablename FROM pg_tables where schemaname = 'public'").fetch().all().map { it["tablename"]!! }
        .collectList().block()

    assertThat(tables).contains("plan")
  }
}
