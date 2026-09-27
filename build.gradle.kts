import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    checkstyle
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.14.0"
}

group = "net.pamytno"
version = "0.0.1-SNAPSHOT"
description = "Памятно — backend для изучения материалов с интервальным повторением"

val versions = mapOf(
    "springModulith" to "1.4.13",
    "archunit" to "1.5.1",
    "checkstyle" to "14.1.0",
    "springdoc" to "2.8.17",
    "mapstruct" to "1.6.3",
    "lombokMapstructBinding" to "0.2.0",
    "pdfbox" to "3.0.8",
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

    // MATERIALS
    implementation("org.apache.pdfbox:pdfbox:${versions["pdfbox"]}")

    // PERSISTENCE
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // HELPERS
    compileOnly("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:${versions["mapstruct"]}")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstruct"]}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${versions["lombokMapstructBinding"]}")

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
============== OpenAPI: contract-first REST ==============
──────────────────────────────────────────────────────
 Каждый openapi/<module>-api.yaml генерирует интерфейсы в net.pamytno.<module>.rest.api
 и DTO в net.pamytno.<module>.rest.dto. Контроллеры модулей реализуют эти интерфейсы.
*/

val openApiOutput = layout.buildDirectory.dir("generated/openapi")
val openApiSpecs = file("openapi").listFiles { file -> file.name.endsWith("-api.yaml") }.orEmpty().sortedBy { it.name }

val openApiTasks = openApiSpecs.map { spec ->
    val module = spec.name.removeSuffix("-api.yaml")
    tasks.register<GenerateTask>("openApiGenerate${module.replaceFirstChar(Char::uppercase)}") {
        description = "Генерирует REST-интерфейсы и DTO модуля $module из ${spec.name}."
        group = "openapi"
        generatorName = "spring"
        inputSpec = spec.absolutePath
        outputDir = openApiOutput.get().dir(module).asFile.absolutePath
        apiPackage = "net.pamytno.$module.rest.api"
        modelPackage = "net.pamytno.$module.rest.dto"
        schemaMappings = mapOf("ErrorResponse" to "net.pamytno.common.error.ErrorResponse")
        typeMappings = mapOf("DateTime" to "Instant")
        importMappings = mapOf("Instant" to "java.time.Instant")
        // Только интерфейсы и модели, без ApiUtil, README и прочих supporting files
        globalProperties = mapOf("apis" to "", "models" to "", "modelDocs" to "false", "apiDocs" to "false")
        configOptions = mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "openApiNullable" to "false",
            "useBeanValidation" to "true",
            "documentationProvider" to "springdoc",
            "hideGenerationTimestamp" to "true",
            "sourceFolder" to "src/main/java",
        )
    }
}

sourceSets.main {
    openApiSpecs.forEach { spec ->
        java.srcDir(openApiOutput.map { it.dir("${spec.name.removeSuffix("-api.yaml")}/src/main/java") })
    }
}

tasks.compileJava {
    dependsOn(openApiTasks)
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
