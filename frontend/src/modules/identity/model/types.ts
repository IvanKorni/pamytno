import type { components } from '../api/schema.gen'

/** Профиль пользователя. */
export type User = components['schemas']['UserDto']

/** Access-токен, выданный при входе или регистрации. */
export type AuthToken = components['schemas']['AuthTokenDto']
