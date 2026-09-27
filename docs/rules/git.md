# Правила git

- Ветка по умолчанию — `main`, удалённый репозиторий — `git@github.com:IvanKorni/pamytno.git`.
- **Коммиты маленькие и ясные**: один коммит — одно логическое изменение
  (миграция + сущность + репозиторий — можно вместе; эндпоинт — отдельно; рефакторинг — отдельно).
- Сообщение — Conventional Commits на русском, повелительное наклонение:
  ```
  feat(topic): добавить загрузку PDF

  Необязательное тело: зачем и какие решения приняты.
  ```
- Типы: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `chore`. Scope — модуль (`common`, `identity`,
  `topic`, `deck`, `learning`) или область (`architecture`, `checkstyle`).
- Код, тесты и документация фичи — в одном коммите.
- Перед коммитом зелёные `./gradlew checkstyleMain checkstyleTest test`; перед пушем — `./gradlew check`.
- Секреты (`.env`, ключи API) не коммитятся.
