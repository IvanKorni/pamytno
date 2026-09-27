import { json, request } from '@/shared'
import type { AuthToken, User } from '../model/types'

/** Регистрирует пользователя и сразу выдаёт access-токен. */
export const register = (email: string, password: string) =>
  request<AuthToken>('/auth/register', json('POST', { email, password }))

/** Входит по email и паролю. */
export const login = (email: string, password: string) =>
  request<AuthToken>('/auth/login', json('POST', { email, password }))

/** Загружает профиль пользователя по текущему токену. */
export const getCurrentUser = () => request<User>('/users/me')
