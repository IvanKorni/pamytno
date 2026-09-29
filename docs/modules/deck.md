# Модуль `deck` — вопросы и карточки

Строит фрагменты единого текста темы, генерирует по ним вопросы через AI, даёт пользователю
отобрать вопросы и создаёт по одобренным вопросам карточки. Карточки английских слов AI составляет сразу
по вставленному тексту, без вопросов. Зависит только от `common`:
о темах узнаёт из событий модуля `topic`.

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `domain` | `TopicRef` | тема и владелец — всё, что `deck` знает о теме |
| `domain` | `TopicMaterial` | проекция: владелец темы и последняя известная версия её текста (`NO_CONTENT` — текста ещё не было) |
| `domain` | `TextChunk`, `ChunkSpan` | неизменяемый фрагмент версии текста со смещениями |
| `domain` | `TextChunker` | деление текста: абзац → строка → предложение → жёсткий разрез |
| `domain` | `GenerationJob`, `GenerationJobType`, `GenerationJobStatus` | асинхронная задача генерации: вопросы, карточки, слова |
| `domain` | `Question`, `QuestionStatus` | вопрос с цитатой-источником и ссылкой на фрагмент; после карточки не меняется |
| `domain` | `QuestionDecision` | решение «изучать / не изучать» |
| `domain` | `Flashcard` | карточка; цитата-источник переходит от вопроса; у карточки слова вопроса нет; редактируема |
| `domain` | `WordEntry` | выражение от модели до проверки: полное предложение и форма выражения в нём |
| `domain` | `WordCard` | карточка слова: проверка ответа модели, раскладка по сторонам |
| `domain` | `WordGap` | пропуск на месте формы выражения с подсказкой — первыми двумя буквами (`ma_____`) |
| `repository` | `TopicMaterialRepository`, `TextChunkRepository` | проекция и фрагменты |
| `repository` | `GenerationJobRepository`, `QuestionRepository`, `FlashcardRepository` | задачи, вопросы, карточки |
| `service` | `TopicMaterialService` | регистрация темы, тема владельца (`requireTopic`), приём новой версии текста (идемпотентно), фрагменты актуальной версии |
| `service` | `GenerationJobService` | запуск (одна задача вида на тему), завершение, чтение задач |
| `service` | `QuestionGenerationService`, `QuestionGenerationRequested` | проверка текста и постановка задачи |
| `service` | `QuestionGenerationWorker` | вызовы AI по фрагментам вне транзакции, логи старта/финиша/длительности |
| `service` | `QuestionWriter` | сохранение вопросов фрагмента |
| `service` | `QuestionQueryService` | вопросы темы с фильтром по статусу, вопрос владельца |
| `service` | `QuestionReviewService` | решение поштучно и массово (всё или ничего), правка формулировки |
| `service` | `CardGenerationService`, `CardGenerationRequested` | проверка одобренных вопросов и постановка задачи |
| `service` | `CardGenerationWorker` | вызовы AI по вопросам вне транзакции |
| `service` | `CardContextBuilder` | контекст карточки: цитата + фрагмент единого текста |
| `service` | `FlashcardWriter` | сохранение карточки (по вопросу — с `CARD_CREATED`, слова — без вопроса), событие `FlashcardCreated` |
| `service` | `VocabularyGenerationService`, `VocabularyGenerationRequested` | проверка владельца темы и постановка задачи слов |
| `service` | `VocabularyGenerationWorker` | один вызов AI на текст вне транзакции, карточка на каждое полное выражение |
| `service` | `FlashcardQueryService` | карточки темы, карточка владельца |
| `service` | `FlashcardCommandService` | правка (`FlashcardUpdated`) и удаление (`FlashcardDeleted`) |
| `service` | `DeckCleanupService` | удаление всех данных темы (идемпотентно) |
| `listener` | `TopicCreatedListener` | `@ApplicationModuleListener` на `TopicCreated` |
| `listener` | `TopicContentPreparedListener` | `@ApplicationModuleListener` на `TopicContentPrepared` |
| `listener` | `QuestionGenerationRequestedListener` | `@Async` после коммита запускает воркер вопросов |
| `listener` | `CardGenerationRequestedListener` | `@Async` после коммита запускает воркер карточек |
| `listener` | `VocabularyGenerationRequestedListener` | `@Async` после коммита запускает воркер слов |
| `listener` | `TopicDeletedListener` | `@ApplicationModuleListener` на `TopicDeleted` |
| `exception` | `TopicContentNotReadyException` | 409 `TOPIC_CONTENT_NOT_READY` |
| `exception` | `GenerationInProgressException` | 409 `GENERATION_IN_PROGRESS` |
| `exception` | `GenerationJobNotFoundException` | 404 `GENERATION_JOB_NOT_FOUND` |
| `exception` | `QuestionNotFoundException` | 404 `QUESTION_NOT_FOUND` |
| `exception` | `QuestionAlreadyHasCardException` | 409 `QUESTION_ALREADY_HAS_CARD` |
| `exception` | `NoApprovedQuestionsException` | 409 `NO_APPROVED_QUESTIONS` |
| `exception` | `CardNotFoundException` | 404 `CARD_NOT_FOUND` |
| `exception` | `TopicNotFoundException` | 404 `TOPIC_NOT_FOUND` — тема неизвестна модулю или чужая |
| `mapper` | `GenerationJobMapper` | задача → DTO |
| `mapper` | `QuestionMapper` | вопрос → DTO, статус и решение из запроса |
| `mapper` | `FlashcardMapper` | карточка → DTO |
| `rest` | `QuestionGenerationRestControllerV1` | `POST /api/topics/{id}/questions/generate` |
| `rest` | `GenerationJobRestControllerV1` | `GET /api/generation-jobs/{id}` |
| `rest` | `QuestionRestControllerV1` | список, правка, approve/reject, массовое решение |
| `rest` | `CardGenerationRestControllerV1` | `POST /api/topics/{id}/cards/generate` |
| `rest` | `VocabularyGenerationRestControllerV1` | `POST /api/topics/{id}/vocabulary/generate` |
| `rest` | `FlashcardRestControllerV1` | список, карточка, правка, удаление |
| `config` | `DeckProperties` | `pamytno.deck.chunk-size` |
| `config` | `AiProperties` | провайдер AI, вопросов на фрагмент, настройки Claude и CLI |
| `integration.ai` | `AiProvider` | интерфейс модели из ТЗ: `generateQuestions`, `generateCard`, `generateVocabulary` |
| `integration.ai` | `GeneratedQuestion`, `GeneratedCard`, `GeneratedWord` | ответы модели |
| `integration.ai` | `VocabularyRules` | общие правила карточек слов: выбор по инструкции, B1, пропуск, переводы |
| `integration.ai` | `AiGenerationException` | модель не справилась |
| `integration.ai` | `AiFormatRules` | общие правила оформления для промптов: вопрос-задача с вариантами, разметка карточки |
| `integration.ai.stub` | `StubAiProvider` | детерминированная заглушка без сети (`AI_PROVIDER=stub`, всегда в тестах): слово — на выделение `**…**` или строку |
| `integration.ai.anthropic` | `AnthropicAiProvider` | Claude через Anthropic Java SDK, structured outputs, проверка `stop_reason` |
| `integration.ai.anthropic` | `AnthropicPrompts` | инструкции; материал в XML-тегах |
| `integration.ai.anthropic` | `QuestionsPayload`, `CardPayload`, `VocabularyPayload` | JSON-схемы ответа модели |
| `integration.ai.cli` | `CliAiProvider` | генерация через локальный `claude` или `codex` CLI (`AI_PROVIDER=cli`), разбор JSON из ответа |
| `integration.ai.cli` | `CliProcessRunner` | запуск CLI без shell с таймаутом, возврат ответа модели |
| `config` | `AnthropicConfig` | `AnthropicClient` из `ANTHROPIC_API_KEY` (только при `AI_PROVIDER=anthropic`) |

Контракт: `openapi/deck-api.yaml`.

## Таблицы (схема `deck`)

| Таблица | Назначение |
|---|---|
| `topic_materials` | `id` = id темы, владелец и последняя версия текста (`0` — тема создана, текста ещё нет) |
| `text_chunks` | фрагменты по версиям; старые версии хранятся — на них ссылаются вопросы |
| `generation_jobs` | задачи генерации вопросов, карточек и карточек слов (`type`) |
| `questions` | вопросы; `chunk_id` → `text_chunks`; порядок — `seq` |
| `flashcards` | карточки; `question_id` → `questions` (у карточек слов пусто); порядок — `seq` |

## События

| Направление | Событие | Реакция |
|---|---|---|
| слушает | `TopicCreated` | запоминает владельца темы без текста |
| слушает | `TopicContentPrepared` | строит фрагменты новой версии |
| слушает | `TopicDeleted` | удаляет карточки, вопросы, задачи, фрагменты и проекцию темы |
| публикует | `FlashcardCreated(cardId, topicId, userId, front, back)` | карточка создана |
| публикует | `FlashcardUpdated(cardId, topicId, userId, front, back)` | карточка изменена |
| публикует | `FlashcardDeleted(cardId, topicId, userId)` | карточка удалена |

## Эндпоинты

| Метод | Путь | Ответ |
|---|---|---|
| POST | `/api/topics/{topicId}/questions/generate` | 202 `GenerationJobDto` |
| GET | `/api/generation-jobs/{jobId}` | 200 `GenerationJobDto` |
| GET | `/api/topics/{topicId}/questions?status=` | 200 `QuestionDto[]` |
| PATCH | `/api/questions/{questionId}` | 200 `QuestionDto` |
| POST | `/api/questions/{questionId}/approve` | 200 `QuestionDto` |
| POST | `/api/questions/{questionId}/reject` | 200 `QuestionDto` |
| POST | `/api/questions/decisions` | 200 `QuestionDto[]` |
| POST | `/api/topics/{topicId}/cards/generate` | 202 `GenerationJobDto` |
| POST | `/api/topics/{topicId}/vocabulary/generate` | 202 `GenerationJobDto` |
| GET | `/api/topics/{topicId}/cards` | 200 `FlashcardDto[]` |
| GET | `/api/cards/{cardId}` | 200 `FlashcardDto` |
| PATCH | `/api/cards/{cardId}` | 200 `FlashcardDto` |
| DELETE | `/api/cards/{cardId}` | 204 |

## Настройки

| Свойство | Переменная окружения | По умолчанию |
|---|---|---|
| `pamytno.deck.chunk-size` | `AI_CHUNK_SIZE` | `6000` символов |
| `pamytno.deck.ai.provider` | `AI_PROVIDER` | `cli` (`anthropic` — Claude, `stub` — заглушка; в тестах всегда `stub`) |
| `pamytno.deck.ai.questions-per-chunk` | `AI_QUESTIONS_PER_CHUNK` | `8` |
| `pamytno.deck.ai.anthropic.model` | `AI_MODEL` | `claude-opus-5` |
| `pamytno.deck.ai.anthropic.max-tokens` | `AI_MAX_TOKENS` | `16000` |
| `pamytno.deck.ai.anthropic.effort` | `AI_EFFORT` | пусто (по умолчанию API) |
| `pamytno.deck.ai.anthropic.refusal-fallback` | `AI_REFUSAL_FALLBACK` | `true` |
| — | `ANTHROPIC_API_KEY` | ключ Anthropic, только из окружения |
