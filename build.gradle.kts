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
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.1")
  runtimeOnly("org.postgresql:r2dbc-postgresql")

  testImplementation("org.testcontainers:postgresql:1.19.3")
  testImplementation("org.assertj:assertj-core:3.25.2")
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
