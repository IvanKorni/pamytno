# 003. Источники материалов

## Сценарий

1. Пользователь добавляет в тему источник: текст, список слов, PDF или видео YouTube.
2. Backend сразу отвечает `202` с источником в статусе `UPLOADED`, тема переходит в `PROCESSING`.
3. Асинхронно извлекается текст; фронт опрашивает `GET /api/sources/{id}` до `READY` или `ERROR`.
4. После обработки пересчитывается статус темы и её единый текст (фича 004).

## API

| Метод | Путь | Успех | Ошибки |
|---|---|---|---|
| POST | `/api/topics/{topicId}/sources/text` | 202 | 400, 401, 404 |
| POST | `/api/topics/{topicId}/sources/pdf` (multipart `file`, `name`) | 202 | 400, 401, 404, 413 |
| POST | `/api/topics/{topicId}/sources/youtube` `{url, name}` | 202 | 400, 401, 404 |
| GET | `/api/topics/{topicId}/sources` | 200 | 401, 404 |
| GET | `/api/sources/{sourceId}` | 200 | 401, 404 |
| GET | `/api/sources/{sourceId}/text` | 200 | 401, 404 |
| DELETE | `/api/sources/{sourceId}` | 204 | 401, 404 |

## Правила и решения

- **Исходный материал никогда не перезаписывается**: `originalText`/`originalUrl`/`originalName` задаются при
  создании (колонки `updatable = false`), результат обработки — отдельно в `extractedText`.
- Список слов (`WORD_LIST`) — тоже текстовый источник: строки, слова, пары «слово — перевод».
- Техническая очистка (`TextCleaner`): переводы строк → `\n`, удаление служебных и невидимых символов,
  схлопывание пробелов, удаление пустых строк. Смысл не меняется, AI не участвует.
- Обработка: внутреннее событие `SourceSubmitted` → `@Async @TransactionalEventListener` после коммита →
  извлечение вне транзакции → запись результата короткой транзакцией → пересчёт темы под блокировкой строки.
- Статус темы: нет источников — `DRAFT`; хоть один в обработке — `PROCESSING`; есть готовый — `READY`;
  все с ошибкой — `ERROR`.
- Удаление источника пересчитывает тему без него.
- **PDF**: проверяется сигнатура `%PDF-`, файл сохраняется в `STORAGE_ROOT/<topicId>/<sourceId>.pdf`
  (в Docker — volume), текстовый слой извлекает PDFBox, OCR нет. Слова, разорванные переносом в конце строки,
  склеиваются (только для PDF, чтобы не портить «из-за» в обычном тексте). Лимит размера — `MAX_UPLOAD_SIZE`.
- **YouTube**: ссылка проверяется сразу (`INVALID_YOUTUBE_URL`), `originalUrl` хранит её как есть. Субтитры берутся
  через внутреннее API YouTube (как у открытых библиотек): страница видео → ключ innertube → ответ плеера со списком
  дорожек → XML дорожки. Выбор: ручные субтитры в `YOUTUBE_LANGUAGES` (по порядку) → автоматические в этих языках →
  любые. Собственного распознавания речи нет (в будущем — Whisper). YouTube может менять внутреннее API —
  тогда источник получит `TRANSCRIPT_UNAVAILABLE`, а исправление локализовано в `integration.youtube`.
- XML субтитров читается как UTF-8 независимо от заголовка ответа; DOCTYPE запрещён (XXE).
- Файлы удаляются из хранилища **после коммита** удаления источника или темы (внутреннее событие
  `StoragePathObsolete`), чтобы откат не оставил запись без файла.

## Ошибки

| Код | Где | Когда |
|---|---|---|
| `TOPIC_NOT_FOUND` | HTTP 404 | тема чужая или не существует |
| `SOURCE_NOT_FOUND` | HTTP 404 | источник чужой или не существует |
| `UNSUPPORTED_FILE_TYPE` | HTTP 400 | загружен не PDF |
| `FILE_TOO_LARGE` | HTTP 413 | файл больше `MAX_UPLOAD_SIZE` |
| `TEXT_EXTRACTION_FAILED` | `source.errorCode` | после очистки текста не осталось; PDF из картинок, битый или с паролем |
| `INVALID_YOUTUBE_URL` | HTTP 400 | ссылка не ведёт на видео YouTube |
| `TRANSCRIPT_UNAVAILABLE` | `source.errorCode` | у видео нет субтитров, видео недоступно, YouTube ответил ошибкой |
| `SOURCE_PROCESSING_FAILED` | `source.errorCode` | непредвиденная ошибка обработки |

## Тесты

- Unit: `SourceTest`, `TopicStatusPolicyTest`, `TextCleanerTest`, `SourceTextExtractorsTest`,
  `SourceProcessingServiceTest`, `PdfTextReaderTest`, `LocalFileStorageTest`, `PdfSourceSubmissionServiceTest`, `YoutubeVideoIdTest`, `CaptionTrackSelectorTest`,
  `CaptionTracksParserTest`, `TranscriptXmlParserTest`, `YoutubeTranscriptClientTest` (WireMock без Spring).
- Модульные: `TextSourceModuleTest` — приём и обработка текста, сохранность исходного текста, ошибка пустого
  текста, список слов, изоляция пользователей, удаление источника; `PdfSourceModuleTest` — PDF с текстом,
  PDF без текстового слоя, не-PDF, удаление файла из хранилища; `YoutubeSourceModuleTest` — субтитры через WireMock,
  видео без субтитров, неверная ссылка.
