# 001. Регистрация и вход

## Сценарий

1. Пользователь регистрируется по email и паролю и сразу получает access-токен.
2. Позже входит по email и паролю и получает новый токен.
3. Все остальные запросы отправляет с `Authorization: Bearer <token>`.

## API

| Метод | Путь | Успех | Ошибки |
|---|---|---|---|
| POST | `/api/auth/register` | 201 `{accessToken, tokenType, expiresIn, userId}` | 400, 403, 409 |
| POST | `/api/auth/login` | 200 `{accessToken, tokenType, expiresIn, userId}` | 400, 401 |
| GET | `/api/users/me` | 200 `{id, email, createdAt}` | 401, 404 |

## Правила и решения

- Email нормализуется (trim + нижний регистр) и уникален.
- Пароль 8–72 символа (72 — предел BCrypt), хранится только BCrypt-хеш, в логи не попадает.
- JWT: HS256, секрет из `JWT_SECRET`, `sub` = `userId`, срок — `JWT_TTL` (12 ч по умолчанию). Refresh-токенов в MVP нет.
- Регистрацию можно выключить: `pamytno.identity.registration-enabled=false` (для личного развёртывания —
  после создания своего пользователя).
- Вход не раскрывает, что именно неверно — email или пароль.

## Ошибки

| Код | Когда |
|---|---|
| `VALIDATION_FAILED` | неверный формат email, короткий пароль |
| `EMAIL_ALREADY_REGISTERED` | email уже занят |
| `REGISTRATION_DISABLED` | регистрация выключена |
| `INVALID_CREDENTIALS` | неверный email или пароль |
| `UNAUTHORIZED` | нет или просрочен токен |
| `USER_NOT_FOUND` | пользователь из токена удалён |

## Тесты

- Unit: `UserTest`, `RegistrationServiceTest`, `LoginServiceTest`, `AccessTokenIssuerTest`.
- Модульные: `IdentityModuleTest` — регистрация, конфликт email, валидация, вход, профиль по токену.
- Интеграционные: вход используется в сквозном сценарии `LearningJourneyIntegrationTest`.
