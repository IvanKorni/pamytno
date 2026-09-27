# Правила тестирования

Тесты пишутся вместе с кодом фичи, в том же коммите. Фича без тестов не считается сделанной.

## Уровни

| Уровень | Что проверяет | Инструменты | Gradle-задача | Тег |
|---|---|---|---|---|
| Unit | домен, политики, сервисы, парсеры — без Spring | JUnit 5, Mockito, AssertJ | `test` | — |
| Архитектура | изоляция модулей, слои, стиль | Spring Modulith, ArchUnit | `test` | — |
| Модульный | один модуль целиком: REST + БД + события | `@ModuleTest` (Modulith `@ApplicationModuleTest`), Testcontainers, MockMvc, `Scenario` | `moduleTest` | `module` |
| Интеграционный | сквозные сценарии через HTTP по всем модулям | `@IntegrationTest` (`@SpringBootTest` RANDOM_PORT), Testcontainers | `integrationTest` | `integration` |

- `./gradlew test` — быстро, без Docker.
- `./gradlew check` — всё: Checkstyle + unit + module + integration (нужен Docker).

## Что покрывать

- **Unit** — каждое правило домена и каждая ветка сервиса (успех, «не найдено», конфликт, ошибка внешней системы).
- **Модульный** — каждый эндпоинт модуля (успех + основная ошибка) и каждый обработчик интеграционных событий.
  Модульный тест поднимает **только свой модуль** и `common` — так проверяется, что модуль живёт сам по себе.
  События других модулей публикуются через `Scenario`, а не через их бины.
- **Интеграционный** — сценарии, проходящие через несколько модулей (полный путь из ТЗ §28 —
  `LearningJourneyIntegrationTest`). HTTP — через `JourneyClient`: он работает как фронт, с токеном пользователя.

## Оформление

- Тестовый класс: `<Класс>Test` (unit), `<Модуль|Фича>ModuleTest` (модульный), `<Сценарий>IntegrationTest`.
- Имя метода: `method_expected_whenCondition`, обязательно `@DisplayName` на русском
  (проверяет `ArchitectureRulesTest`).
- Тело — блоки `// given`, `// when`, `// then`.
- Утверждения — AssertJ (`assertThat`).
- Асинхронность — Awaitility, никогда `Thread.sleep`.
- События, опубликованные **в потоке теста** (синхронно в запросе), проверяются через `AssertablePublishedEvents`.
  События из **асинхронных** обработчиков — только через `CapturedEvents.await(...)`: `Scenario` и `PublishedEvents`
  Spring Modulith привязаны к потоку (InheritableThreadLocal) и теряют события из уже созданных потоков пула.
- Время в модульных и интеграционных тестах — `TestClock` (подменяет `Clock` приложения): чтобы проверить
  «через день карточка снова к повторению», тест делает `clock.advance(...)` и сбрасывает сдвиг в `@AfterEach`.
- Внешние HTTP-системы (YouTube, LLM) — WireMock или заглушка `AiProvider`; реальная сеть в тестах запрещена.
  Общий WireMock-сервер — `TestWireMock`, его адрес прописывает `TestEnvironmentInitializer`;
  заглушки разных тестов различаются уникальными идентификаторами (например, случайный `videoId`).
- Тестовые данные собираются фабриками в `src/test/java/.../support`.
