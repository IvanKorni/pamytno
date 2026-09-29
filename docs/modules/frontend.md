# Frontend

Frontend реализует пользовательский путь «Памятно» (в интерфейсе — «Напоминатор», `APP_NAME`): авторизация → тема → материалы → вопросы → карточки →
обучение → прогресс. React + TypeScript + Vite, server state — TanStack Query, пользователь и уведомления — Zustand,
состояние одного экрана — локальный `useState` / `useReducer`.

## Слои

Зависимости идут только сверху вниз: `app → pages → modules → shared`.

| Слой | Что лежит | Может импортировать |
|---|---|---|
| `src/app` | корень, маршруты, каркас приложения, стили | всё |
| `src/pages` | экраны по адресам, собирают модули вместе | `modules`, `shared` |
| `src/modules/<module>` | модуль по контракту backend: `identity`, `topic`, `deck`, `learning` | только `shared` |
| `src/shared` | HTTP-клиент, UI-кит, форматирование | ничего из слоёв выше |

Модули **не импортируют друг друга** — как и на backend. Нужны данные двух модулей — их собирает страница.
Снаружи модуль доступен только через свой `index.ts`, внутренние файлы — приватные. `index.ts` открывает
хуки и компоненты для страниц и HTTP-функции модуля (`topicApi`, `deckApi`, …) для интеграционного теста.
Между слоями импорты идут через алиас `@/`, внутри модуля — относительные.

## shared

| Файл | Ответственность |
|---|---|
| `api/client.ts` | `request` с токеном и разбором JSON, `HttpError`, событие 401 |
| `api/tokenStorage.ts` | хранение access-токена в localStorage |
| `lib/format.ts` | проценты и склонение слов |
| `lib/richText.ts` | разбор разметки карточек и вопросов: абзацы, списки, варианты ответа, код, выделение |
| `lib/sessionValues.ts` | значения в sessionStorage, безопасно при недоступном хранилище |
| `lib/useKeyboardShortcuts.ts` | горячие клавиши экрана; не срабатывают в полях, на кнопках и в окнах |
| `ui/Brand.tsx` | название приложения `APP_NAME` («Напоминатор») и логотип |
| `ui/Modal.tsx` | модальное окно с Esc и управлением фокусом |
| `ui/EmptyState.tsx`, `ui/ErrorState.tsx`, `ui/Loading.tsx` | пустое состояние, ошибка загрузки и строка ошибки `InlineError`, загрузка |
| `ui/ProcessingCard.tsx` | долгая операция backend крупным текстом |
| `ui/RichText.tsx` | `RichText` и `RichInline`: текст с разметкой из React-элементов, без HTML |
| `ui/Segmented.tsx` | переключатель режимов экрана (`aria-pressed`) |
| `ui/toast.ts`, `ui/ToastContainer.tsx` | всплывающие уведомления |

## modules/identity

| Файл | Ответственность |
|---|---|
| `api/identityApi.ts` | регистрация, вход, профиль |
| `model/authStore.ts` | текущий пользователь и выход |
| `model/useAuthBootstrap.ts` | восстановление входа по токену, выход только по 401, повтор при недоступном сервере |
| `model/useLogout.ts` | выход с очисткой кеша запросов |
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
| `ui/TopicContentPanel.tsx` | единый текст темы под материалами, только чтение |
| `ui/CreateTopicModal.tsx` | окно новой темы |
| `ui/EditTopicModal.tsx` | настройки и удаление темы |

## modules/deck

| Файл | Ответственность |
|---|---|
| `api/deckApi.ts` | вопросы, решения, генерация, карточки слов, карточки |
| `api/deckKeys.ts` | ключи кеша модуля |
| `api/questionQueries.ts` | вопросы, решения с подменой в кеше, опрос задачи генерации |
| `api/cardQueries.ts` | карточки, изменение и удаление |
| `model/generationJobs.ts` | интервал опроса, ошибка задачи и запоминание идущих задач |
| `model/selection.ts` | выбор вопросов: только новые, без уже решённых |
| `model/useGenerationRun.ts` | запуск генерации (с данными или без) и опрос задачи до `READY` / `ERROR`; `launchThen` — переход после старта; ошибка запуска отдельно от ошибки задачи |
| `model/useQuestionGeneration.ts` | генерация вопросов темы — со вкладки вопросов и со вкладки материалов |
| `model/useQuestionsWorkflow.ts` | сценарий отбора вопросов и генерации карточек |
| `model/useVocabularyGeneration.ts` | составление карточек слов: запуск по тексту, опрос, перечитывание карточек и уведомление с итогом |
| `model/wordsDraft.ts` | черновик окна слов: режимы «все / жирные / коллокации / своя», инструкция для AI, запрос |
| `model/pastedText.ts` | вставка из HTML: жирное (теги и `font-weight`, кроме обёртки Google Docs) → `**слово**`, абзацы → переносы |
| `model/labels.ts` | подписи статусов вопроса |
| `ui/QuestionReview.tsx`, `ui/QuestionList.tsx` | отбор по одному (в том числе стрелками) и списком |
| `ui/FlashcardTile.tsx` | карточка для просмотра: первая строка вопроса и короткая вторая (предложение с пропуском у карточки слова), по нажатию — остальное и ответ |
| `ui/CardViewer.tsx` | `CardViewer` — карточки по одной с «Назад» / «Далее», `CardGrid` — все сеткой |
| `ui/EditCardModal.tsx` | изменение и удаление карточки |
| `ui/AddWordsModal.tsx`, `ui/WordsFields.tsx` | окно карточек слов: вкладки режима, своя инструкция, текст со вставкой жирного |

## modules/learning

| Файл | Ответственность |
|---|---|
| `api/learningApi.ts` | dashboard, прогресс, карточки к повторению, сессии, ответы |
| `api/learningKeys.ts` | ключи кеша модуля |
| `api/learningQueries.ts` | dashboard, прогресс темы и его перечитывание после изменения карточек |
| `model/learningSession.ts` | состояние учебной сессии: очередь, ответы, возврат забытых карточек |
| `model/useLearningSession.ts` | старт сессии без кеша, ответы и завершение на backend |
| `ui/LearningCard.tsx` | карточка в режиме обучения; пробел — ответ, ← / → — «Не помню» / «Помню» |
| `ui/LearningComplete.tsx` | итоги сессии |
| `ui/ProgressOverview.tsx` | крупный процент, показатели и распределение карточек |

## pages и app

| Файл | Ответственность |
|---|---|
| `pages/AuthPage.tsx` | вход и регистрация |
| `pages/dashboard/*` | обзор: плитки тем, строка «Сегодня», создание темы в окне |
| `pages/topic/TopicLayout.tsx`, `TopicHeader.tsx` | путь, шапка темы с прогрессом и действиями, вкладки, окна темы; составление карточек слов живёт здесь и видно всем вкладкам |
| `pages/topic/topicActions.ts` | действия шапки темы и составление карточек слов для вкладок (`useOutletContext`) |
| `pages/topic/MaterialsPage.tsx`, `MaterialActions.tsx` | материалы, «Создать вопросы» или «К вопросам», исходный текст |
| `pages/topic/QuestionsPage.tsx`, `QuestionsFooter.tsx` | отбор вопросов, действия под ними, итог генерации карточек |
| `pages/topic/CardsPage.tsx`, `CardsBrowser.tsx` | ход составления карточек слов, карточки по одной или все, поиск, изменение и удаление |
| `pages/topic/ProgressPage.tsx` | прогресс темы |
| `pages/learning/LearningPage.tsx`, `LearningTopBar.tsx` | режим обучения на весь экран: тема, номер карточки, полоса пройденного |
| `pages/topic/useTopicId.ts` | идентификатор темы из адреса |
| `app/App.tsx` | маршруты, восстановление входа, возврат на исходный адрес после входа |
| `app/queryClient.ts` | кеш запросов: свежесть и повтор только сетевых ошибок и 5xx |
| `app/AppShell.tsx`, `app/useScrolled.ts` | каркас: шапка (прозрачная до прокрутки), без шапки — режим обучения |
| `app/styles/*.css` | стили по областям экрана; `index.css` задаёт порядок каскада |

## Тесты и архитектурные проверки

| Файл | Ответственность |
|---|---|
| `test/fakeBackend.ts` | фейковый backend: подмена `fetch`, таблица маршрутов, запись запросов |
| `test/renderApp.tsx` | всё приложение в StrictMode от имени пользователя или гостя |
| `test/fixtures.ts` | тестовые темы, вопросы, карточки, задачи и сессии |
| `test/setup.ts` | очистка DOM, хранилищ и сторов между тестами |
| `app/learningJourney.integration.test.ts` | путь ТЗ §28 через API-слой фронта и настоящий backend |
| `architecture/sourceFiles.ts` | разбор исходников TypeScript для архитектурных тестов |
| `architecture/*.unit.test.ts` | изоляция модулей, правила кода, JSDoc на русском, соглашения тестов |

## Скрипты

| Файл | Ответственность |
|---|---|
| `scripts/generate-api.mjs` | типы DTO из `openapi/*.yaml`; `--check` падает на устаревших типах |
| `scripts/seed-demo.mjs` | демо-пользователь с темами, вопросами и карточками для ручной проверки |

## Эндпоинты

Все запросы идут через `/api` (в dev Vite проксирует на `localhost:8080`) по контрактам `openapi/*.yaml`.
Типы DTO генерируются в `src/modules/<module>/api/schema.gen.ts` командой `npm run api:generate`.
