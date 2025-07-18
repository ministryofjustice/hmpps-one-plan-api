import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  val kotlinVersion = "2.2.0"
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.2"
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
  implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.8")
  implementation("org.jsoup:jsoup:1.21.1")

  runtimeOnly("org.flywaydb:flyway-core:11.10.3")
  runtimeOnly("org.flywaydb:flyway-database-postgresql:11.10.3")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.7")
  runtimeOnly("org.postgresql:r2dbc-postgresql")

  testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.3"))
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:localstack")
  testImplementation("org.assertj:assertj-core:3.27.3")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
  testImplementation("io.mockk:mockk:1.14.5")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.awaitility:awaitility:4.3.0")
  testImplementation(kotlin("reflect"))
}

kotlin {
  jvmToolchain(21)
}

tasks.named<BootRun>("bootRun") {
  systemProperty("spring.profiles.active", project.findProperty("profiles")?.toString() ?: "local")
}
