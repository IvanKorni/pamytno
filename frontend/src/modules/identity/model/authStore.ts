import { create } from 'zustand'
import { tokenStorage } from '@/shared'
import type { User } from './types'

/** Почему пользователь вышел: сам нажал «Выйти» или истёк токен. */
export type LogoutReason = 'signOut' | 'expired'

/** Состояние авторизации: текущий пользователь, выход и его причина. */
interface AuthState {
  user?: User
  /** Пользователь вышел сам — возвращать следующего вошедшего на прежний адрес не нужно. */
  signedOut: boolean
  setUser: (user?: User) => void
  logout: (reason: LogoutReason) => void
}

/** Текущий пользователь; токен хранится отдельно в `tokenStorage`. */
export const useAuth = create<AuthState>((set) => ({
  signedOut: false,
  setUser: (user) => set({ user, signedOut: false }),
  logout: (reason) => {
    tokenStorage.clear()
    set({ user: undefined, signedOut: reason === 'signOut' })
  },
}))
