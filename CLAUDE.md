# Памятно — backend

Spring Boot 3.5 модульный монолит (Java 21, Spring Modulith, PostgreSQL, Flyway, Gradle Kotlin DSL).
ТЗ — `docs/tz-backend.md`. Стиль кода — как в проекте `proselyte-system`.

## Команды

```bash
docker compose up -d postgres        # БД для локального запуска (порт 5433)
./gradlew bootRun                    # запуск приложения (профиль local)
docker compose up -d --build         # БД + backend в Docker (настройки из .env)
./gradlew checkstyleMain checkstyleTest test   # быстро: стиль + unit + архитектура
./gradlew moduleTest                 # модульные тесты (Docker)
./gradlew integrationTest            # сквозные тесты (Docker)
./gradlew check                      # всё вместе — перед пушем
```

## Главное

- Модули `identity`, `topic`, `deck`, `learning` **не зависят друг от друга** — только от `common`.
  Общаются событиями из `common.event`. Это проверяют `ModularityTest` и `ModuleIsolationTest`.
- Каждый класс, метод и константа enum — с Javadoc на русском. Классы и методы маленькие (лимиты в Checkstyle).
- REST — contract-first: `openapi/<module>-api.yaml` → сгенерированные интерфейсы → `*RestControllerV1`.
- На каждую фичу: unit + модульный + (если сквозная) интеграционный тест и md-карточка в `docs/`.
- Коммиты маленькие, Conventional Commits на русском.
- Для новой фичи используй скилл `pamytno-feature`.

## Правила (обязательны, дополняются по ходу проекта)

@docs/rules/architecture.md
@docs/rules/code-style.md
@docs/rules/testing.md
@docs/rules/documentation.md
@docs/rules/git.md
