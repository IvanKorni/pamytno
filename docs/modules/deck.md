# Модуль `deck` — вопросы и карточки

Строит фрагменты единого текста темы, генерирует по ним вопросы через AI, даёт пользователю
отобрать вопросы и создаёт по одобренным вопросам карточки. Зависит только от `common`:
о темах узнаёт из событий модуля `topic`.

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `domain` | `TopicRef` | тема и владелец — всё, что `deck` знает о теме |
| `domain` | `TopicMaterial` | проекция: последняя известная версия единого текста темы |
| `domain` | `TextChunk`, `ChunkSpan` | неизменяемый фрагмент версии текста со смещениями |
| `domain` | `TextChunker` | деление текста: абзац → строка → предложение → жёсткий разрез |
| `repository` | `TopicMaterialRepository`, `TextChunkRepository` | проекция и фрагменты |
| `service` | `TopicMaterialService` | приём новой версии текста (идемпотентно), фрагменты актуальной версии |
| `listener` | `TopicContentPreparedListener` | `@ApplicationModuleListener` на `TopicContentPrepared` |
| `config` | `DeckProperties` | `pamytno.deck.chunk-size` |
| `config` | `AiProperties` | провайдер AI, вопросов на фрагмент, настройки Claude |
| `integration.ai` | `AiProvider` | интерфейс модели из ТЗ: `generateQuestions`, `generateCard` |
| `integration.ai` | `GeneratedQuestion`, `GeneratedCard` | ответы модели |
| `integration.ai` | `AiGenerationException` | модель не справилась |
| `integration.ai.stub` | `StubAiProvider` | детерминированная заглушка без сети (`AI_PROVIDER=stub`) |

Контракт: `openapi/deck-api.yaml`.

## Таблицы (схема `deck`)

| Таблица | Назначение |
|---|---|
| `topic_materials` | `id` = id темы, владелец и последняя версия текста |
| `text_chunks` | фрагменты по версиям; старые версии хранятся — на них ссылаются вопросы |

## События

| Направление | Событие | Реакция |
|---|---|---|
| слушает | `TopicContentPrepared` | строит фрагменты новой версии |

## Настройки

| Свойство | Переменная окружения | По умолчанию |
|---|---|---|
| `pamytno.deck.chunk-size` | `AI_CHUNK_SIZE` | `6000` символов |
| `pamytno.deck.ai.provider` | `AI_PROVIDER` | `stub` (`anthropic` — Claude) |
| `pamytno.deck.ai.questions-per-chunk` | `AI_QUESTIONS_PER_CHUNK` | `8` |
| `pamytno.deck.ai.anthropic.model` | `AI_MODEL` | `claude-opus-5` |
| `pamytno.deck.ai.anthropic.max-tokens` | `AI_MAX_TOKENS` | `16000` |
| `pamytno.deck.ai.anthropic.effort` | `AI_EFFORT` | пусто (по умолчанию API) |
