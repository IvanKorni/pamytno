# Правила архитектуры

## Модульный монолит

- Приложение — один Spring Boot монолит на Spring Modulith.
- Модуль — прямой подпакет `net.pamytno` с `package-info.java`:
  ```java
  @ApplicationModule(displayName = "…", allowedDependencies = "common")
  package net.pamytno.<module>;
  ```
- Модули: `identity`, `topic`, `deck`, `learning`. Общее ядро — `common` (shared module, тип `OPEN`).
- **Модули не ходят друг в друга.** Модуль зависит только от `common`. Никаких импортов классов
  другого модуля, никаких вызовов его бинов, никаких SQL-запросов к его таблицам.
- Проверяют тесты `ModularityTest` (Spring Modulith `verify()`) и `ModuleIsolationTest` (ArchUnit).
  Эти тесты не отключаются и не ослабляются.

## Взаимодействие модулей — только события

- Интеграционные события лежат в `net.pamytno.common.event.<модуль-источник>`.
- Событие — `record` в прошедшем времени (`TopicDeleted`, `FlashcardCreated`), содержит только
  идентификаторы и данные, без JPA-сущностей.
- Публикация — `ApplicationEventPublisher` внутри транзакции сервиса.
- Приём — `@ApplicationModuleListener` (асинхронно, после коммита, в новой транзакции).
  События сохраняются в реестре Spring Modulith (`event_publication`) и переотправляются после рестарта.
- Нужны данные другого модуля — модуль хранит **локальную проекцию** и обновляет её по событиям
  (пример: `deck.text_chunks` строится из `TopicContentPrepared`).
- Долгие операции (PDF, YouTube, AI) — внутреннее событие модуля + `@Async @TransactionalEventListener`
  без транзакции; запись результата — короткими транзакциями в сервисах.

## Данные

- Каждый модуль владеет своей схемой PostgreSQL: `identity`, `topic`, `deck`, `learning`.
- Внешних ключей между схемами нет. Ссылки на чужие сущности — просто `uuid`.
- Все изменения схемы — только Flyway: `src/main/resources/db/migration/V<N>__<module>_<действие>.sql`.
- `ddl-auto: validate` — сущности обязаны совпадать с миграциями.

## Пакеты внутри модуля

| Пакет | Что лежит |
|---|---|
| `domain` | JPA-сущности с поведением, enum-ы, value objects, доменные политики |
| `repository` | Spring Data репозитории |
| `service` | прикладные сервисы (сценарии), транзакции |
| `listener` | обработчики событий |
| `rest` | контроллеры `*RestControllerV1`, реализующие сгенерированные интерфейсы |
| `rest.api`, `rest.dto` | **генерируются** из `openapi/<module>-api.yaml`, руками не правятся |
| `mapper` | MapStruct-мапперы сущность ⇄ DTO |
| `integration` | внешние системы: PDF, YouTube, LLM, файловое хранилище |
| `exception` | исключения модуля (наследники из `common.error`) |
| `config` | `@Configuration` и `@ConfigurationProperties` модуля |

Направление зависимостей: `rest → service → domain/repository/integration`.
Контроллер не трогает репозитории, домен не знает про Spring Web и сервисы.

## REST API

- Contract-first: сначала `openapi/<module>-api.yaml`, потом код. Контроллер реализует сгенерированный интерфейс.
- Пути — `/api/...`. Ошибки — единый `ErrorResponse {code, message, timestamp}` из `common.error`.
- Текущий пользователь — `CurrentUser` из `common.security`. Любой запрос к данным фильтруется по `userId`;
  чужой ресурс отвечает `404`, а не `403`.
- Долгие операции отвечают `202 Accepted` и статусом (`PROCESSING / READY / ERROR`), фронт опрашивает.

## Ошибки

- Исключения модулей наследуют `NotFoundException`, `ConflictException`, `BadRequestException`,
  `UnauthorizedException` из `common.error` и задают стабильный `code` (`TOPIC_NOT_FOUND`).
- `GlobalExceptionHandler` в `common` — единственное место, где исключения превращаются в HTTP-ответ.

## Время, конфигурация, секреты

- Текущее время — только через внедрённый `java.time.Clock` (правило в `ArchitectureRulesTest`).
- Настройки — `@ConfigurationProperties` record-ы с префиксом `pamytno.<module>`.
- Секреты (JWT, ключи LLM) — только из переменных окружения, в репозиторий не попадают.
