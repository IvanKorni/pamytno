# Frontend

Frontend реализует пользовательский путь «Памятно»: авторизация → тема → материалы → вопросы → карточки →
обучение → прогресс. React + TypeScript + Vite, server state — TanStack Query, маленький client state — Zustand.

## Слои

Зависимости идут только сверху вниз: `app → pages → modules → shared`.

| Слой | Что лежит | Может импортировать |
|---|---|---|
| `src/app` | корень, маршруты, каркас приложения, глобальные стили | всё |
| `src/pages` | экраны по адресам, собирают модули вместе | `modules`, `shared` |
| `src/modules/<module>` | модуль по контракту backend: `identity`, `topic`, `deck`, `learning` | только `shared` |
| `src/shared` | HTTP-клиент, UI-кит, форматирование | ничего из слоёв выше |

Модули **не импортируют друг друга** — как и на backend. Нужны данные двух модулей — их собирает страница.
Снаружи модуль доступен только через свой `index.ts`, внутренние файлы — приватные.
Между слоями импорты идут через алиас `@/`, внутри модуля — относительные.

## shared

| Файл | Ответственность |
|---|---|
| `api/client.ts` | `request` с токеном и разбором JSON, `HttpError`, событие 401 |
| `api/tokenStorage.ts` | хранение access-токена в localStorage |
| `lib/format.ts` | проценты и склонение слов |
| `ui/Modal.tsx` | модальное окно |
| `ui/EmptyState.tsx`, `ui/ErrorState.tsx`, `ui/InlineError.tsx`, `ui/Loading.tsx` | пустое состояние, ошибка, загрузка |
| `ui/ProcessingCard.tsx` | карточка долгой операции backend |
| `ui/Stat.tsx` | показатели в строке и в карточке |
| `ui/toast.ts`, `ui/ToastContainer.tsx` | всплывающие уведомления |

## modules/identity

| Файл | Ответственность |
|---|---|
| `api/identityApi.ts` | регистрация, вход, профиль |
| `model/authStore.ts` | текущий пользователь и выход |
| `model/useAuthBootstrap.ts` | восстановление входа по токену и выход по 401 |
| `model/useSignIn.ts` | вход или регистрация с сохранением токена |

## modules/topic

| Файл | Ответственность |
|---|---|
| `api/topicApi.ts`, `api/sourceApi.ts` | темы, источники и единый текст |
| `api/topicKeys.ts` | ключи кеша модуля |
| `api/topicQueries.ts` | запросы и мутации тем |
| `api/sourceQueries.ts` | источники с опросом обработки, единый текст, добавление и удаление |
| `model/materialDraft.ts` | черновик материала и отправка в нужный эндпоинт |
| `model/sourcePolling.ts` | интервал опроса источников в обработке |
| `model/labels.ts` | подписи статусов и типов |
| `ui/SourceRow.tsx` | строка источника |
| `ui/AddMaterialModal.tsx`, `ui/MaterialTypePicker.tsx`, `ui/MaterialFields.tsx` | добавление материала |
| `ui/TopicContentModal.tsx` | единый текст темы |
| `ui/EditTopicModal.tsx` | настройки и удаление темы |

## modules/deck

| Файл | Ответственность |
|---|---|
| `api/deckApi.ts` | вопросы, решения, генерация, карточки |
| `api/deckKeys.ts` | ключи кеша модуля |
| `api/questionQueries.ts` | вопросы и опрос задачи генерации |
| `api/cardQueries.ts` | карточки, изменение и удаление |
| `model/useQuestionsWorkflow.ts` | сценарий отбора вопросов и генерации карточек |
| `model/labels.ts` | подписи статусов вопроса |
| `ui/QuestionReview.tsx`, `ui/QuestionList.tsx` | отбор по одному и списком |
| `ui/FlashcardRow.tsx`, `ui/EditCardModal.tsx` | карточка в списке и её изменение |

## modules/learning

| Файл | Ответственность |
|---|---|
| `api/learningApi.ts` | dashboard, прогресс, карточки к повторению, сессии, ответы |
| `api/learningKeys.ts` | ключи кеша модуля |
| `api/learningQueries.ts` | dashboard и прогресс темы |
| `model/learningStore.ts` | очередь текущей сессии |
| `model/useLearningFlow.ts` | сценарий учебной сессии |
| `ui/LearningCard.tsx` | карточка в режиме обучения |
| `ui/LearningComplete.tsx` | итоги сессии |
| `ui/ProgressOverview.tsx` | кольцо и полосы прогресса |

## pages и app

| Файл | Ответственность |
|---|---|
| `pages/AuthPage.tsx` | вход и регистрация |
| `pages/CreateTopicPage.tsx` | создание темы |
| `pages/dashboard/*` | обзор: «Сегодня» и карточки тем с прогрессом |
| `pages/topic/TopicLayout.tsx` | шапка темы, статистика, вкладки |
| `pages/topic/MaterialsPage.tsx`, `QuestionsPage.tsx`, `CardsPage.tsx`, `LearningPage.tsx`, `ProgressPage.tsx` | экраны темы |
| `pages/topic/useTopicId.ts` | идентификатор темы из адреса |
| `app/App.tsx` | маршруты и восстановление входа |
| `app/AppShell.tsx` | боковое меню и каркас |

## Эндпоинты

Все запросы идут через `/api` (в dev Vite проксирует на `localhost:8080`) по контрактам `openapi/*.yaml`.
Типы DTO генерируются в `src/modules/<module>/api/schema.gen.ts` командой `npm run api:generate`.
