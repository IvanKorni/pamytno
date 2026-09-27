# Модуль `common` — общее ядро

Shared-модуль Spring Modulith (тип `OPEN`): от него зависят все модули, сам он не знает ни об одном модуле.
Здесь только то, что действительно общее: формат ошибок, безопасность, время, база сущностей
и контракты интеграционных событий.

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `common` | `package-info` | объявление shared-модуля |
| `config` | `ClockConfig`, `TimeProperties` | бин `Clock` в поясе `pamytno.time.zone` |
| `config` | `AsyncConfig` | включает `@Async` |
| `config` | `OpenApiConfig` | описание API и схема Bearer JWT для Swagger UI |
| `domain` | `BaseEntity` | UUID при создании, `Persistable` без лишнего `merge` |
| `error` | `ErrorResponse` | единый формат ошибки `{code, message, timestamp}` |
| `error` | `ErrorCodes` | общие коды ошибок |
| `error` | `ApplicationException` | база исключений с кодом |
| `error` | `NotFoundException` / `ConflictException` / `BadRequestException` / `UnauthorizedException` / `ForbiddenException` | категории → 404 / 409 / 400 / 401 / 403 |
| `error` | `ErrorResponseFactory` | сборка тела и HTTP-ответа ошибки |
| `error` | `ApplicationExceptionHandler` | исключения приложения → HTTP |
| `error` | `ValidationExceptionHandler` | ошибки валидации и разбора тела → 400 |
| `error` | `HttpExceptionHandler` | 404 пути, 405, 413 |
| `error` | `UnexpectedExceptionHandler` | всё остальное → 500 без деталей |
| `security` | `SecurityConfig` | stateless JWT, публичные пути, CORS |
| `security` | `JwtProperties`, `CorsProperties` | настройки безопасности |
| `security` | `JwtDecoderConfig` | проверка подписи, срока и издателя токена |
| `security` | `SecurityErrorWriter` | 401/403 от Spring Security в едином формате |
| `security` | `CurrentUser` | `userId` из claim `sub` |

## Настройки

| Свойство | Переменная окружения | По умолчанию |
|---|---|---|
| `pamytno.time.zone` | `APP_TIME_ZONE` | `UTC` |
| `pamytno.security.jwt.secret` | `JWT_SECRET` | — (обязателен, ≥ 32 символов) |
| `pamytno.security.jwt.ttl` | `JWT_TTL` | `12h` |
| `pamytno.security.cors.allowed-origins` | `CORS_ALLOWED_ORIGINS` | `http://localhost:*` |

## Таблицы

`public.event_publication` — реестр событий Spring Modulith (миграция `V1`).

## Тесты

- Unit: `ErrorResponseFactoryTest`, `ApplicationExceptionHandlerTest`, `CurrentUserTest`.
- Модульный: `CommonModuleTest` — 401/404 в едином формате, публичные health и api-docs.
