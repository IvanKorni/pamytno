# Модуль `topic` — темы и материалы

Темы пользователя, источники материалов (PDF, YouTube, текст, список слов), извлечение текста
и сборка единого текста темы (Master Text). Зависит только от `common`.

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `domain` | `Topic`, `TopicStatus` | тема и её состояние |
| `domain` | `TopicStatusPolicy` | статус темы по статусам источников |
| `domain` | `Source`, `SourceType`, `SourceStatus`, `SourceErrorCode` | источник: исходный материал неизменен, извлечённый текст отдельно |
| `domain` | `TextCleaner` | техническая очистка текста без изменения смысла |
| `repository` | `TopicRepository` | темы пользователя, блокировка строки для пересборки |
| `repository` | `SourceRepository` | источники темы и пользователя |
| `service` | `TopicQueryService` | список тем, тема владельца (чужая = 404) |
| `service` | `TopicCommandService` | создание и частичное изменение темы |
| `service` | `TopicDeletionService` | удаление темы + событие `TopicDeleted` |
| `service` | `SourceSubmissionService`, `SourceSubmitted` | приём источника, тема → PROCESSING, внутреннее событие на обработку |
| `service` | `SourceProcessingService` | извлечение текста вне транзакции, запись результата, пересчёт темы |
| `service` | `SourceStateService` | короткие транзакции смены состояния источника |
| `service` | `TopicMaterialRefresher` | пересчёт темы под блокировкой строки |
| `service` | `SourceQueryService`, `SourceDeletionService` | чтение и удаление источников |
| `service.extraction` | `SourceTextExtractor`, `SourceTextExtractors` | стратегии извлечения + очистка и проверка на пустоту |
| `service.extraction` | `PlainTextExtractor` | TEXT и WORD_LIST |
| `service.extraction` | `TextExtractionException` | причина ошибки, записывается в источник |
| `listener` | `SourceSubmittedListener` | `@Async` после коммита запускает обработку |
| `exception` | `TopicNotFoundException` | 404 `TOPIC_NOT_FOUND` |
| `exception` | `SourceNotFoundException` | 404 `SOURCE_NOT_FOUND` |
| `mapper` | `TopicMapper`, `SourceMapper` | сущности → DTO |
| `rest` | `TopicRestControllerV1` | `/api/topics` |
| `rest` | `TextSourceRestControllerV1` | `/api/topics/{id}/sources/text` |
| `rest` | `SourceRestControllerV1` | список, статус, тексты и удаление источников |

Контракт: `openapi/topic-api.yaml`.

## Таблицы (схема `topic`)

| Таблица | Назначение |
|---|---|
| `topics` | темы; `user_id` — владелец без внешнего ключа на `identity` |
| `sources` | источники; `original_*` не перезаписываются, `extracted_text` — результат |

## События

| Направление | Событие | Когда |
|---|---|---|
| публикует | `TopicDeleted(topicId, userId)` | тема удалена |

## Эндпоинты

| Метод | Путь | Ответ |
|---|---|---|
| GET | `/api/topics` | 200 `TopicDto[]` |
| POST | `/api/topics` | 201 `TopicDto` |
| GET | `/api/topics/{topicId}` | 200 `TopicDto` |
| PATCH | `/api/topics/{topicId}` | 200 `TopicDto` |
| DELETE | `/api/topics/{topicId}` | 204 |
| GET | `/api/topics/{topicId}/sources` | 200 `SourceDto[]` |
| POST | `/api/topics/{topicId}/sources/text` | 202 `SourceDto` |
| GET | `/api/sources/{sourceId}` | 200 `SourceDto` |
| GET | `/api/sources/{sourceId}/text` | 200 `SourceTextDto` |
| DELETE | `/api/sources/{sourceId}` | 204 |
