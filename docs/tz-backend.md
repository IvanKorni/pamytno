# Памятно — техническое задание на Backend

## 1. Цель

Разработать backend веб-приложения «Памятно» для загрузки учебных материалов, преобразования их в единый текст, генерации вопросов и карточек с помощью AI и последующего интервального повторения.

На первом этапе приложение является личным проектом и рассчитано преимущественно на одного пользователя, однако структура данных должна позволять в будущем добавить полноценную многопользовательскую систему.

---

## 2. Технологический стек

Основной стек:

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Docker
- Docker Compose
- Maven или Gradle
- REST API

Дополнительно:

- хранение загруженных файлов локально на сервере через Docker Volume;
- интеграция с внешним LLM API;
- интеграция с YouTube для получения текста/субтитров;
- Swagger / OpenAPI для документации REST API.

Не использовать:

- Kubernetes;
- микросервисную архитектуру;
- Kafka;
- сложную event-driven архитектуру;
- отдельный API Gateway;
- распределённое файловое хранилище.

Backend представляет собой один Spring Boot монолит.

---

# 3. Основная предметная модель

Основной объект приложения — **Topic / Тема**.

Тема представляет собой набор материалов, объединённых пользователем для изучения.

Пример:

«Spring Security»

Внутри темы могут находиться:

- PDF;
- YouTube-видео;
- текст;
- список слов;
- несколько разных источников одновременно.

Все источники преобразуются в текст и образуют единый корпус материала темы.

---

# 4. Основные сущности

## User

На MVP достаточно одного пользователя.

Тем не менее рекомендуется сразу иметь сущность:

- id
- email
- passwordHash
- createdAt
- updatedAt

На первом этапе можно создать одного пользователя вручную или через простую регистрацию.

---

## Topic

Поля:

- id
- userId
- title
- description
- status
- createdAt
- updatedAt

Статусы:

- DRAFT
- PROCESSING
- READY
- ERROR

---

## Source

Отдельный источник информации.

Поля:

- id
- topicId
- type
- originalName
- originalUrl
- originalText
- extractedText
- status
- createdAt

Типы:

- PDF
- YOUTUBE
- TEXT
- WORD_LIST

Статусы:

- UPLOADED
- PROCESSING
- READY
- ERROR

### Важное требование

Исходный текст никогда не должен перезаписываться после обработки.

Необходимо хранить отдельно:

- оригинальный материал;
- извлечённый текст.

---

# 5. Master Text

После обработки всех Source внутри Topic формируется единый текст темы.

Сущность:

## TopicContent

Поля:

- id
- topicId
- content
- version
- createdAt

Master Text формируется путём объединения текстов всех Source.

На MVP не требуется сложная нормализация или переписывание текста.

AI не должен автоматически сокращать, пересказывать или изменять исходный материал.

Допускается только техническая очистка:

- удаление служебных символов;
- исправление очевидных проблем извлечения PDF;
- удаление пустых строк;
- нормализация пробелов.

Смысл и содержание текста изменяться не должны.

---

# 6. Загрузка материалов

Backend должен поддерживать следующие варианты.

## PDF

Endpoint принимает PDF-файл.

После загрузки:

1. файл сохраняется;
2. создаётся Source;
3. из PDF извлекается текст;
4. текст сохраняется в extractedText;
5. обновляется Master Text темы.

Для MVP можно использовать Apache PDFBox.

OCR в первой версии не требуется.

Если PDF состоит из изображений и текст извлечь невозможно, Source получает ошибку:

`TEXT_EXTRACTION_FAILED`.

---

## Обычный текст

Пользователь отправляет текст через API.

Текст сохраняется без изменения.

---

## Список слов

Можно передать:

- строки;
- слова;
- пары слово-перевод;
- произвольный текст.

На backend это также является текстовым источником.

---

## YouTube

Пользователь отправляет URL.

Backend должен попытаться получить доступные субтитры / transcript.

Результат сохраняется как extractedText.

Если transcript получить невозможно, источник получает статус ERROR.

На MVP не требуется собственное распознавание аудио.

В будущем можно добавить Whisper.

---

# 7. Генерация вопросов

После того как Topic содержит текст, пользователь запускает:

`Generate questions`

Backend передаёт части Master Text в LLM.

Текст может быть разбит на chunks из-за ограничений контекста модели.

LLM должен создавать потенциальные вопросы для изучения.

---

## Question

Поля:

- id
- topicId
- text
- sourceFragment
- status
- createdAt

Статусы:

- GENERATED
- APPROVED
- REJECTED
- CARD_CREATED

`sourceFragment` должен содержать фрагмент материала, на основании которого был создан вопрос.

Это позволит позднее:

- показывать пользователю источник;
- проверять корректность AI;
- повторно генерировать карточку.

---

# 8. Отбор вопросов пользователем

Frontend показывает сгенерированные вопросы.

Пользователь для каждого вопроса выбирает:

- изучать;
- не изучать.

API должен позволять:

- approve question;
- reject question;
- массово approve/reject несколько вопросов.

Только APPROVED вопросы используются для генерации карточек.

---

# 9. Генерация карточек

После подтверждения вопросов пользователь запускает создание карточек.

LLM получает:

- вопрос;
- связанный sourceFragment;
- при необходимости дополнительный контекст из Master Text.

Создаётся Flashcard.

---

## Flashcard

Поля:

- id
- topicId
- questionId
- front
- back
- sourceFragment
- createdAt
- updatedAt

Карточка может иметь разный объём.

Примеры:

Короткая:

Front:
`Что такое JVM?`

Back:
`JVM — виртуальная машина, выполняющая Java bytecode.`

Расширенная:

Front:
`Как работает Spring Security Filter Chain?`

Back:
несколько предложений с объяснением темы.

На MVP не вводить жёсткое ограничение формата карточки.

Рекомендуемое максимальное ограничение ответа:

около 10 предложений.

---

# 10. Редактирование карточек

Пользователь должен иметь возможность:

- изменить вопрос;
- изменить ответ;
- удалить карточку.

AI-generated карточка никогда не должна быть неизменяемой.

---

# 11. Режим просмотра карточек

Backend должен поддерживать получение:

### всех карточек темы

`GET /topics/{topicId}/cards`

и карточек, которые необходимо повторить:

`GET /topics/{topicId}/reviews/due`

---

# 12. Интервальное повторение

Для каждой карточки хранится состояние обучения.

## CardProgress

Поля:

- id
- cardId
- userId
- stage
- nextReviewAt
- lastReviewAt
- consecutiveSuccess
- totalReviews
- totalRemembered
- totalForgotten

---

# 13. Интервалы повторения

Начальная схема:

Stage 0 → сейчас

Stage 1 → +1 день

Stage 2 → +3 дня

Stage 3 → +7 дней

Stage 4 → +14 дней

Stage 5 → +30 дней

После успешного прохождения Stage 5 карточка считается изученной, но остаётся доступна пользователю.

Позднее алгоритм может быть заменён на FSRS или другой spaced repetition algorithm.

---

# 14. Ответы пользователя

В режиме обучения пользователь может выбрать:

### REMEMBER

Карточка успешно вспомнена.

Переходит на следующий stage.

`nextReviewAt` рассчитывается согласно расписанию.

---

### FORGOT

Пользователь не вспомнил карточку.

На MVP:

- stage уменьшается;
- карточка возвращается в текущую learning session;
- nextReviewAt пересчитывается.

Рекомендуемая простая логика:

если stage > 1:

`stage = stage - 1`

иначе:

`stage = 0`.

---

### CONTINUE

Не является оценкой памяти.

Используется для перехода к следующей части карточки / продолжения просмотра.

Если карточка не имеет дополнительных частей, может просто закрывать раскрытый ответ.

---

# 15. Learning Session

Желательно создать сущность:

## LearningSession

Поля:

- id
- userId
- topicId
- startedAt
- completedAt
- cardsTotal
- cardsRemembered
- cardsForgotten

Это позволит собирать статистику.

---

# 16. Прогресс темы

Backend должен рассчитывать:

- всего карточек;
- новых карточек;
- карточек на изучении;
- карточек, ожидающих повторения;
- изученных карточек;
- процент изучения;
- количество карточек к повторению сегодня.

Пример ответа:

```json
{
  "totalCards": 120,
  "newCards": 20,
  "learningCards": 45,
  "masteredCards": 55,
  "dueToday": 12,
  "progress": 45.8
}
```

---

# 17. Основные REST API

Примерная структура.

## Auth

`POST /api/auth/login`

`POST /api/auth/register`

---

## Topics

`GET /api/topics`

`POST /api/topics`

`GET /api/topics/{id}`

`PATCH /api/topics/{id}`

`DELETE /api/topics/{id}`

---

## Sources

`POST /api/topics/{id}/sources/pdf`

`POST /api/topics/{id}/sources/text`

`POST /api/topics/{id}/sources/youtube`

`GET /api/topics/{id}/sources`

`DELETE /api/sources/{id}`

---

## Questions

`POST /api/topics/{id}/questions/generate`

`GET /api/topics/{id}/questions`

`PATCH /api/questions/{id}`

`POST /api/questions/{id}/approve`

`POST /api/questions/{id}/reject`

---

## Cards

`POST /api/topics/{id}/cards/generate`

`GET /api/topics/{id}/cards`

`GET /api/cards/{id}`

`PATCH /api/cards/{id}`

`DELETE /api/cards/{id}`

---

## Learning

`GET /api/topics/{id}/reviews/due`

`POST /api/cards/{id}/review`

Пример:

```json
{
  "result": "REMEMBER"
}
```

или:

```json
{
  "result": "FORGOT"
}
```

---

## Progress

`GET /api/topics/{id}/progress`

`GET /api/dashboard`

---

# 18. AI Service

Интеграцию с LLM необходимо вынести в отдельный слой.

Например:

```text
AiService
 ├── QuestionGenerationService
 └── CardGenerationService
```

Не привязывать бизнес-логику приложения напрямую к конкретному API.

Интерфейс:

```java
interface AiProvider {
    List<GeneratedQuestion> generateQuestions(String text);

    GeneratedCard generateCard(
        String question,
        String context
    );
}
```

Это позволит позднее использовать:

- OpenAI;
- Claude;
- Gemini;
- локальную LLM.

---

# 19. Работа с длинными текстами

Master Text может превышать context window модели.

Необходимо предусмотреть TextChunkService.

Он разбивает текст на части.

Каждый chunk должен иметь:

- sequenceNumber;
- startOffset;
- endOffset;
- content.

При генерации вопроса необходимо сохранять связь с исходным chunk.

---

# 20. Асинхронные операции

Операции:

- PDF parsing;
- YouTube processing;
- генерация вопросов;
- генерация карточек

могут занимать длительное время.

На MVP Kafka не требуется.

Использовать:

- Spring `@Async`;
- task executor;
- статусы PROCESSING / READY / ERROR.

Frontend периодически проверяет состояние задачи.

В дальнейшем можно добавить SSE/WebSocket.

---

# 21. Ошибки

API должно возвращать единый формат ошибки.

```json
{
  "code": "SOURCE_PROCESSING_FAILED",
  "message": "Unable to extract text from PDF",
  "timestamp": "..."
}
```

---

# 22. База данных

Использовать PostgreSQL.

Все изменения структуры БД делать через Flyway migrations.

Основные таблицы:

```text
users
topics
sources
topic_contents
text_chunks
questions
flashcards
card_progress
learning_sessions
```

---

# 23. Docker

Backend должен запускаться через Docker Compose.

Минимально:

```text
backend
postgres
```

Пример архитектуры production:

```text
Internet
   ↓
Reverse Proxy
   ↓
React frontend
   ↓
Spring Boot backend
   ↓
PostgreSQL
```

Загруженные файлы хранятся в Docker Volume.

---

# 24. Local Development

Для локальной разработки backend должен запускаться непосредственно из IDE.

Например:

```text
IntelliJ IDEA
     ↓
Spring Boot
     ↓
PostgreSQL Docker
```

PostgreSQL можно поднимать отдельно через docker-compose.

---

# 25. Логи

Использовать стандартный Spring / SLF4J logging.

Логировать:

- ошибки обработки файлов;
- запуск AI generation;
- завершение AI generation;
- ошибки внешних API;
- длительность обработки.

Не логировать:

- пароли;
- API keys;
- полный пользовательский документ.

---

# 26. Безопасность

На MVP:

- JWT Authentication;
- BCrypt passwords;
- API keys LLM только через environment variables;
- ограничение размера загружаемых файлов.

---

# 27. Что НЕ входит в MVP

Не требуется:

- мобильное приложение;
- социальные функции;
- команды;
- совместное обучение;
- marketplace карточек;
- публичные темы;
- сложный adaptive learning;
- FSRS;
- OCR;
- обработка аудио;
- собственная LLM;
- Kubernetes;
- микросервисы;
- Kafka;
- Redis;
- отдельный search engine;
- vector database / RAG.

---

# 28. Критерий готовности Backend MVP

Backend считается готовым, если пользователь может пройти полный сценарий:

```text
Создать тему
↓
Добавить PDF / YouTube / текст
↓
Получить единый текст
↓
Запустить генерацию вопросов
↓
Одобрить вопросы
↓
Создать карточки
↓
Начать обучение
↓
Нажать Remember / Forgot
↓
Получить правильную следующую дату повторения
↓
Вернуться через некоторое время
↓
Получить карточки, срок повторения которых наступил
```