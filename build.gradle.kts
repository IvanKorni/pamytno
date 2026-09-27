plugins {
    java
    checkstyle
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "net.pamytno"
version = "0.0.1-SNAPSHOT"
description = "Памятно — backend для изучения материалов с интервальным повторением"

val versions = mapOf(
    "springModulith" to "1.4.13",
    "archunit" to "1.5.1",
    "checkstyle" to "14.1.0",
    "springdoc" to "2.8.17",
)

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${versions["springModulith"]}")
    }
}

dependencies {
    // SPRING
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springdoc"]}")

    // MODULITH
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")

    // PERSISTENCE
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // HELPERS
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5:${versions["archunit"]}")
    testImplementation("org.awaitility:awaitility")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/*
──────────────────────────────────────────────────────
============== Checkstyle ==============
──────────────────────────────────────────────────────
*/

checkstyle {
    toolVersion = versions.getValue("checkstyle")
    maxWarnings = 0
    isIgnoreFailures = false
}

tasks.checkstyleTest {
    configFile = file("config/checkstyle/checkstyle-test.xml")
}

tasks.withType<Checkstyle> {
    exclude { it.file.absolutePath.contains("${File.separator}build${File.separator}generated${File.separator}") }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

/*
──────────────────────────────────────────────────────
============== Тесты: unit / module / integration ==============
──────────────────────────────────────────────────────
*/

tasks.test {
    description = "Быстрые unit- и архитектурные тесты без Spring-контекста."
    useJUnitPlatform {
        excludeTags("module", "integration")
    }
}

val moduleTest = tasks.register<Test>("moduleTest") {
    description = "Модульные тесты: один модуль Spring Modulith + PostgreSQL в Testcontainers."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("module")
    }
    shouldRunAfter(tasks.test)
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Интеграционные тесты: всё приложение целиком через HTTP."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter(moduleTest)
}

tasks.check {
    dependsOn(moduleTest, integrationTest)
}
