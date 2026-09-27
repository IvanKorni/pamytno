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
| `repository` | `CardProgressRepository` | прогресс по карточке |
| `service` | `CardProgressRegistry` | синхронизация с карточками `deck` (идемпотентно) |
| `listener` | `FlashcardEventsListener` | `FlashcardCreated/Updated/Deleted` |

Контракт: `openapi/learning-api.yaml`.

## Таблицы (схема `learning`)

| Таблица | Назначение |
|---|---|
| `card_progress` | прогресс карточки; `card_id` уникален; `next_review_at` NULL — изучена |

## События

| Направление | Событие | Реакция |
|---|---|---|
| слушает | `FlashcardCreated` | заводит прогресс на этапе 0, к повторению сразу |
| слушает | `FlashcardUpdated` | обновляет копию текста |
| слушает | `FlashcardDeleted` | удаляет прогресс |
