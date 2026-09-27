# Frontend

Frontend реализует пользовательский путь «Памятно»: авторизация → тема → материалы → вопросы → карточки → обучение → прогресс.

| Часть | Ответственность |
|---|---|
| `src/api` | Запросы к identity, topic, deck и learning API |
| `src/store.ts` | JWT-пользователя, learning session и toast-состояние |
| `src/App.tsx` | Маршруты и экраны MVP |
| `src/styles.css` | Адаптивный минималистичный интерфейс |
| `src/*.test.*` | Unit, module и HTTP integration проверки |
