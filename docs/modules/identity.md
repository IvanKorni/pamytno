# Модуль `identity` — пользователи

Регистрация, вход по email и паролю, выпуск JWT и профиль текущего пользователя.
Другие модули о пользователях ничего не знают, кроме `userId` из токена (`CurrentUser`).

## Классы

| Пакет | Класс | Ответственность |
|---|---|---|
| `domain` | `User` | пользователь; нормализует email, хранит только хеш пароля |
| `repository` | `UserRepository` | поиск по email, проверка занятости |
| `service` | `RegistrationService` | регистрация + сразу токен |
| `service` | `LoginService` | проверка пароля + токен |
| `service` | `AccessTokenIssuer`, `AccessToken` | выпуск JWT (`sub`, `email`, `iss`, `exp`) |
| `service` | `UserQueryService` | чтение профиля |
| `config` | `IdentityProperties` | `pamytno.identity.registration-enabled` |
| `config` | `PasswordEncoderConfig` | BCrypt |
| `config` | `JwtEncoderConfig` | подпись HS256 тем же секретом, что проверяет `common` |
| `exception` | `EmailAlreadyRegisteredException` | 409 `EMAIL_ALREADY_REGISTERED` |
| `exception` | `InvalidCredentialsException` | 401 `INVALID_CREDENTIALS` |
| `exception` | `RegistrationDisabledException` | 403 `REGISTRATION_DISABLED` |
| `exception` | `UserNotFoundException` | 404 `USER_NOT_FOUND` |
| `mapper` | `IdentityMapper` | `User`/`AccessToken` → DTO |
| `rest` | `AuthRestControllerV1` | `/api/auth/register`, `/api/auth/login` |
| `rest` | `UserRestControllerV1` | `/api/users/me` |

Контракт: `openapi/identity-api.yaml`.

## Таблицы (схема `identity`)

| Таблица | Назначение |
|---|---|
| `users` | пользователи; уникальный `email` в нижнем регистре |

## События

Не публикует и не слушает.

## Эндпоинты

| Метод | Путь | Ответ |
|---|---|---|
| POST | `/api/auth/register` | 201 `AuthTokenDto` |
| POST | `/api/auth/login` | 200 `AuthTokenDto` |
| GET | `/api/users/me` | 200 `UserDto` |
