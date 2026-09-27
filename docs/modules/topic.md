# Модуль `topic` — темы и материалы

Темы пользователя, источники материалов (PDF, YouTube, текст, список слов), извлечение текста
и сборка единого текста темы (Master Text). Зависит только от `common`.

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `domain` | `Topic`, `TopicStatus` | тема и её состояние |
| `repository` | `TopicRepository` | темы пользователя, блокировка строки для пересборки |
| `service` | `TopicQueryService` | список тем, тема владельца (чужая = 404) |
| `service` | `TopicCommandService` | создание и частичное изменение темы |
| `service` | `TopicDeletionService` | удаление темы + событие `TopicDeleted` |
| `exception` | `TopicNotFoundException` | 404 `TOPIC_NOT_FOUND` |
| `mapper` | `TopicMapper` | тема → DTO |
| `rest` | `TopicRestControllerV1` | `/api/topics` |

Контракт: `openapi/topic-api.yaml`.

## Таблицы (схема `topic`)

| Таблица | Назначение |
|---|---|
| `topics` | темы; `user_id` — владелец без внешнего ключа на `identity` |

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
