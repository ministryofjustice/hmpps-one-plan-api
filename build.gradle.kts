import org.springframework.boot.gradle.tasks.run.BootRun


plugins {
  val kotlinVersion = "1.9.22"
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.1"
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
  implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
  implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

  runtimeOnly("org.flywaydb:flyway-core:10.6.0")
  runtimeOnly("org.flywaydb:flyway-database-postgresql:10.6.0")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.1")
  runtimeOnly("org.postgresql:r2dbc-postgresql")

  testImplementation("org.testcontainers:postgresql:1.19.3")
  testImplementation("org.assertj:assertj-core:3.25.2")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.4")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.4")
  testImplementation("io.mockk:mockk:1.13.9")
  testImplementation("com.ninja-squad:springmockk:4.0.2")

  constraints {
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3") {
      because("CVE-2023-52428")
    }
  }
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}

tasks.named<BootRun>("bootRun") {
  systemProperty("spring.profiles.active", "local")
}
