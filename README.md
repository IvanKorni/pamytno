# Памятно — backend

Backend веб-приложения «Памятно»: загрузка учебных материалов, сборка единого текста темы,
генерация вопросов и карточек с помощью AI и интервальное повторение.

Путь пользователя (ТЗ §28): тема → PDF / YouTube / текст → единый текст → вопросы → отбор →
карточки → повторение «Вспомнил / Не вспомнил» → карточки возвращаются, когда пришёл срок.

## Стек

Java 21 · Spring Boot 3.5 · Spring Modulith · PostgreSQL 17 · Flyway · Spring Security (JWT) ·
OpenAPI (contract-first) · MapStruct · Lombok · PDFBox · Anthropic Java SDK ·
JUnit 5 · Testcontainers · WireMock · ArchUnit · Checkstyle · Gradle (Kotlin DSL).

## Модули

| Модуль | Что делает |
|---|---|
| `identity` | регистрация, вход, JWT |
| `topic` | темы, источники (текст, PDF, YouTube), извлечение текста, единый текст темы |
| `deck` | фрагменты текста, генерация вопросов и карточек через AI, отбор и редактирование |
| `learning` | интервальное повторение, учебные сессии, прогресс и dashboard |
| `common` | ошибки, безопасность, время, интеграционные события |

Модули не зависят друг от друга и общаются только событиями — см. [архитектуру](docs/rules/architecture.md).

## Быстрый старт

Нужны JDK 21 и Docker.

**Всё в Docker:**

```bash
cp .env.example .env    # заполнить JWT_SECRET: openssl rand -base64 48
docker compose up -d --build
```

**Backend из IDE / Gradle, БД в Docker:**

```bash
docker compose up -d postgres
./gradlew bootRun
```

`bootRun` включает профиль `local` с dev-секретом JWT. БД доступна на `localhost:5433`
(5432 часто занят локальным PostgreSQL).

Приложение — `http://localhost:8080`, Swagger UI — `http://localhost:8080/swagger-ui.html`,
health-check — `/actuator/health`.

## Frontend

Фронтенд MVP находится в [`frontend/`](frontend/) и работает с этим API через `/api`.

```bash
cd frontend
npm install
npm run dev
```

Откройте `http://localhost:5173`. Для production-сборки используйте `npm run build`.

```bash
npm run check                  # типы из OpenAPI, ESLint, Prettier, unit + module тесты, сборка
npm run api:generate           # перегенерировать типы DTO после изменения openapi/*.yaml
VITE_PAMYTNO_INTEGRATION_URL=http://localhost:8080 npm run test:integration   # HTTP-путь через backend
```

## Настройки

Всё задаётся переменными окружения, полный список с пояснениями — [`.env.example`](.env.example).

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `JWT_SECRET` | — | секрет подписи JWT, ≥ 32 символов; без него приложение не стартует |
| `AI_PROVIDER` | `stub` | `stub` — заглушка без сети, `anthropic` — Claude |
| `ANTHROPIC_API_KEY` | — | ключ Claude, нужен только при `AI_PROVIDER=anthropic` |
| `AI_MODEL` | `claude-opus-5` | модель Claude |
| `AI_REFUSAL_FALLBACK` | `true` | при отказе модели повторить запрос на запасной модели |
| `APP_TIME_ZONE` | `UTC` | пояс для «сегодня» в прогрессе |
| `STORAGE_ROOT` | `./storage` | где хранятся загруженные PDF |
| `MAX_UPLOAD_SIZE` | `20MB` | максимальный размер PDF |

Секреты в репозиторий не попадают: `.env` в `.gitignore`, ключи и пароли не пишутся в логи.

## Тесты

```bash
./gradlew checkstyleMain checkstyleTest test   # быстро, без Docker: стиль, unit, архитектура
./gradlew moduleTest                           # модульные: один модуль + PostgreSQL (Docker)
./gradlew integrationTest                      # сквозные по HTTP через все модули (Docker)
./gradlew check                                # всё вместе — перед пушем
```

Уровни тестов и правила — [testing.md](docs/rules/testing.md). Внешние сервисы в тестах не вызываются:
YouTube подменяет WireMock, AI — заглушка.

## Документация

- [ТЗ](docs/tz-backend.md)
- Правила проекта: [архитектура](docs/rules/architecture.md), [стиль кода](docs/rules/code-style.md),
  [тесты](docs/rules/testing.md), [документация](docs/rules/documentation.md), [git](docs/rules/git.md)
- Модули — [`docs/modules`](docs/modules), фичи — [`docs/features`](docs/features)
- REST-контракты — [`openapi`](openapi)
- Для Claude Code: [CLAUDE.md](CLAUDE.md) и скилл `pamytno-feature` — порядок добавления фичи
