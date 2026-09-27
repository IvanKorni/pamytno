# Правила frontend

- Frontend находится в `frontend/` и собирается Vite.
- React-компоненты не вызывают `fetch` напрямую: запросы находятся в `src/api/`.
- TanStack Query используется для server state, Zustand — только для небольшого client state.
- Типы DTO генерируются из `openapi/*.yaml` командой `npm run api:generate` в `src/modules/<module>/api/schema.gen.ts`
  и руками не правятся; `npm run check` падает, если контракт изменился, а типы нет (`api:check`).
- Frontend не пересчитывает даты повторения.
- Асинхронные операции обязаны иметь loading, success, error и empty state.
- Чистая логика покрывается unit-тестами, API-границы — module-тестами, HTTP-путь — integration-тестом.
- Перед маленьким коммитом запускаются `npm run check` и соответствующий integration-тест при доступном backend.
- Текст интерфейса и тестовых описаний — на русском; имена кода — на английском.
