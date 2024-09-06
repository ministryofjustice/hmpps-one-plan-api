import org.springframework.boot.gradle.tasks.run.BootRun


plugins {
  val kotlinVersion = "2.0.20"
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.4"
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
}

configurations {
  implementation { exclude(module = "spring-boot-starter-web") }
  implementation { exclude(module = "spring-boot-starter-tomcat") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
  implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")
  implementation("uk.gov.justice.service.hmpps:hmpps-audit-sdk:1.0.0")
  implementation("org.jsoup:jsoup:1.18.1")

  runtimeOnly("org.flywaydb:flyway-core:10.17.3")
  runtimeOnly("org.flywaydb:flyway-database-postgresql:10.17.3")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.4")
  runtimeOnly("org.postgresql:r2dbc-postgresql")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.3.2")
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.1"))
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:localstack")
  testImplementation("org.assertj:assertj-core:3.26.3")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.5")
  testImplementation("io.mockk:mockk:1.13.12")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.awaitility:awaitility:4.2.2")
  testImplementation(kotlin("reflect"))
}

kotlin {
  jvmToolchain(21)
}

tasks.named<BootRun>("bootRun") {
  systemProperty("spring.profiles.active", project.findProperty("profiles")?.toString() ?: "local")
}
