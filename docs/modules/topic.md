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
| `domain` | `TopicContent` | неизменяемая версия единого текста темы |
| `domain` | `MasterTextBuilder` | объединение готовых источников через пустую строку |
| `repository` | `TopicRepository` | темы пользователя, блокировка строки для пересборки |
| `repository` | `SourceRepository` | источники темы и пользователя |
| `repository` | `TopicContentRepository` | последняя версия единого текста |
| `service` | `TopicQueryService` | список тем, тема владельца (чужая = 404) |
| `service` | `TopicCommandService` | создание и частичное изменение темы |
| `service` | `TopicDeletionService` | удаление темы + событие `TopicDeleted` |
| `service` | `SourceRegistrar`, `SourceSubmitted` | общий шаг приёма: сохранить, тема → PROCESSING, внутреннее событие на обработку |
| `service` | `TextSourceSubmissionService` | приём текста и списка слов |
| `service` | `PdfSourceSubmissionService` | проверка сигнатуры `%PDF-`, сохранение файла, регистрация |
| `service` | `StoragePathObsolete` | внутреннее событие: файл/каталог больше не нужен |
| `service` | `SourceProcessingService` | извлечение текста вне транзакции, запись результата, пересчёт темы |
| `service` | `SourceStateService` | короткие транзакции смены состояния источника |
| `service` | `TopicMaterialRefresher` | пересчёт статуса и единого текста темы под блокировкой строки |
| `service` | `TopicContentService` | новая версия текста при изменении + событие `TopicContentPrepared` |
| `service` | `TopicContentQueryService` | последняя версия текста темы владельца |
| `service` | `SourceQueryService`, `SourceDeletionService` | чтение и удаление источников |
| `service.extraction` | `SourceTextExtractor`, `SourceTextExtractors` | стратегии извлечения + очистка и проверка на пустоту |
| `service.extraction` | `PlainTextExtractor` | TEXT и WORD_LIST |
| `service.extraction` | `PdfExtractor` | текстовый слой PDF + склейка переносов |
| `service.extraction` | `TextExtractionException` | причина ошибки, записывается в источник |
| `listener` | `SourceSubmittedListener` | `@Async` после коммита запускает обработку |
| `listener` | `StorageCleanupListener` | удаляет файлы из хранилища после коммита |
| `integration.storage` | `LocalFileStorage` | файлы на диске (Docker volume), защита от выхода за корень |
| `integration.pdf` | `PdfTextReader` | PDFBox, без OCR |
| `config` | `TopicStorageProperties` | `pamytno.topic.storage.root` |
| `exception` | `TopicNotFoundException` | 404 `TOPIC_NOT_FOUND` |
| `exception` | `SourceNotFoundException` | 404 `SOURCE_NOT_FOUND` |
| `exception` | `TopicContentNotFoundException` | 404 `TOPIC_CONTENT_NOT_FOUND` |
| `exception` | `UnsupportedFileTypeException` | 400 `UNSUPPORTED_FILE_TYPE` |
| `mapper` | `TopicMapper`, `SourceMapper`, `TopicContentMapper` | сущности → DTO |
| `rest` | `TopicRestControllerV1` | `/api/topics` |
| `rest` | `TextSourceRestControllerV1` | `/api/topics/{id}/sources/text` |
| `rest` | `PdfSourceRestControllerV1` | `/api/topics/{id}/sources/pdf` (multipart) |
| `rest` | `SourceRestControllerV1` | список, статус, тексты и удаление источников |
| `rest` | `TopicContentRestControllerV1` | `/api/topics/{id}/content` |

Контракт: `openapi/topic-api.yaml`.

## Таблицы (схема `topic`)

| Таблица | Назначение |
|---|---|
| `topics` | темы; `user_id` — владелец без внешнего ключа на `identity` |
| `sources` | источники; `original_*` не перезаписываются, `extracted_text` — результат |
| `topic_contents` | версии единого текста, уникальны по `(topic_id, version)` |

## События

| Направление | Событие | Когда |
|---|---|---|
| публикует | `TopicDeleted(topicId, userId)` | тема удалена |
| публикует | `TopicContentPrepared(topicId, userId, version, content)` | собрана новая версия единого текста |

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
| POST | `/api/topics/{topicId}/sources/pdf` | 202 `SourceDto` |
| GET | `/api/sources/{sourceId}` | 200 `SourceDto` |
| GET | `/api/sources/{sourceId}/text` | 200 `SourceTextDto` |
| DELETE | `/api/sources/{sourceId}` | 204 |
| GET | `/api/topics/{topicId}/content` | 200 `TopicContentDto` |

## Настройки

| Свойство | Переменная окружения | По умолчанию |
|---|---|---|
| `pamytno.topic.storage.root` | `STORAGE_ROOT` | `./storage` |
| `spring.servlet.multipart.max-file-size` | `MAX_UPLOAD_SIZE` | `20MB` |
