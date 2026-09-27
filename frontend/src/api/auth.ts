import { json, request } from './client'
import type { AuthToken, User } from '../types'

export const login = (email: string, password: string) => request<AuthToken>('/auth/login', json('POST', { email, password }))
export const register = (email: string, password: string) => request<AuthToken>('/auth/register', json('POST', { email, password }))
export const getCurrentUser = () => request<User>('/users/me')

