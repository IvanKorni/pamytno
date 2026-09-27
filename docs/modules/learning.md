# Модуль `learning` — обучение

Интервальное повторение карточек, учебные сессии и прогресс. Зависит только от `common`:
о карточках узнаёт из событий `deck`, об удалении тем — из событий `topic`.

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `domain` | `ReviewSchedule` | расписание этапов 0–5 и изученный этап 6; правила REMEMBER/FORGOT |
| `domain` | `CardProgress` | этап, дата следующего повторения, счётчики ответов карточки |
| `domain` | `CardRef`, `CardSnapshot` | ссылка на карточку `deck` и копия её текста |
| `domain` | `ReviewResult` | ответ REMEMBER / FORGOT / CONTINUE |
| `domain` | `LearningSession` | учебная сессия: карточек на старте, вспомнено, забыто |
| `domain` | `ProgressStats` | счётчики прогресса и процент изученных; сумма наборов |
| `domain` | `TopicProgress`, `Dashboard` | прогресс темы; итог и прогресс тем пользователя |
| `repository` | `CardProgressRepository` | прогресс по карточке, очередь повторения, счётчики по темам |
| `repository` | `ProgressCounts` | счётчики темы из одного агрегатного запроса |
| `repository` | `LearningSessionRepository` | сессии пользователя |
| `service` | `CardProgressRegistry` | синхронизация с карточками `deck` (идемпотентно) |
| `service` | `LearningSessionService` | начало, чтение, завершение сессий |
| `service` | `DueReviewService` | очередь повторения темы, самые давние первыми |
| `service` | `ReviewService`, `ReviewOutcome` | ответ по карточке + учёт в сессии той же темы |
| `service` | `ProgressService` | прогресс темы и dashboard; «сегодня» — в поясе приложения |
| `service` | `LearningCleanupService` | удаляет прогресс и сессии удалённой темы |
| `listener` | `FlashcardEventsListener` | `FlashcardCreated/Updated/Deleted` |
| `listener` | `TopicDeletedListener` | `TopicDeleted` (бин `learningTopicDeletedListener`) |
| `exception` | `LearningSessionNotFoundException` | 404 `LEARNING_SESSION_NOT_FOUND` |
| `exception` | `LearningSessionCompletedException` | 409 `LEARNING_SESSION_COMPLETED` |
| `exception` | `LearningSessionTopicMismatchException` | 409 `LEARNING_SESSION_TOPIC_MISMATCH` |
| `exception` | `CardProgressNotFoundException` | 404 `CARD_PROGRESS_NOT_FOUND` |
| `mapper` | `LearningSessionMapper` | сессия → DTO |
| `mapper` | `ReviewMapper` | карточка к повторению и итог ответа → DTO |
| `mapper` | `ProgressMapper` | прогресс темы и dashboard → DTO |
| `rest` | `LearningSessionRestControllerV1` | старт, чтение, завершение сессии |
| `rest` | `ReviewRestControllerV1` | очередь повторения и ответы |
| `rest` | `ProgressRestControllerV1` | прогресс темы и dashboard |

Контракт: `openapi/learning-api.yaml`.

## Таблицы (схема `learning`)

| Таблица | Назначение |
|---|---|
| `card_progress` | прогресс карточки; `card_id` уникален; `next_review_at` NULL — изучена |
| `learning_sessions` | учебные сессии и их статистика |

## События

| Направление | Событие | Реакция |
|---|---|---|
| слушает | `FlashcardCreated` | заводит прогресс на этапе 0, к повторению сразу |
| слушает | `FlashcardUpdated` | обновляет копию текста |
| слушает | `FlashcardDeleted` | удаляет прогресс |
| слушает | `TopicDeleted` | удаляет прогресс карточек и сессии темы (`deck` удаляет карточки темы пачкой, без `FlashcardDeleted`) |

## Эндпоинты

| Метод | Путь | Ответ |
|---|---|---|
| GET | `/api/topics/{topicId}/reviews/due?limit=` | 200 `DueCardDto[]` |
| POST | `/api/cards/{cardId}/review` | 200 `ReviewResultDto` |
| POST | `/api/topics/{topicId}/learning-sessions` | 201 `LearningSessionDto` |
| GET | `/api/learning-sessions/{sessionId}` | 200 `LearningSessionDto` |
| POST | `/api/learning-sessions/{sessionId}/complete` | 200 `LearningSessionDto` |
| GET | `/api/topics/{topicId}/progress` | 200 `TopicProgressDto` |
| GET | `/api/dashboard` | 200 `DashboardDto` |
