# Образ backend «Памятно».
# Сборка только собирает jar; тесты запускаются отдельно: ./gradlew check.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
# Зависимости — отдельным слоем: пересобираются, только когда меняется сборка.
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null
COPY config config
COPY openapi openapi
COPY src/main src/main
RUN ./gradlew --no-daemon --quiet bootJar

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 1001 pamytno \
    && mkdir -p /app/storage \
    && chown pamytno /app/storage
WORKDIR /app
COPY --from=build /workspace/build/libs/pamytno.jar app.jar
USER pamytno
ENV STORAGE_ROOT=/app/storage
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
