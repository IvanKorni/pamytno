import { create } from 'zustand'
import { tokenStorage } from '@/shared'
import type { User } from './types'

/** Состояние авторизации: текущий пользователь и выход. */
interface AuthState {
  user?: User
  setUser: (user?: User) => void
  logout: () => void
}

/** Текущий пользователь; токен хранится отдельно в `tokenStorage`. */
export const useAuth = create<AuthState>((set) => ({
  setUser: (user) => set({ user }),
  logout: () => {
    tokenStorage.clear()
    set({ user: undefined })
  },
}))
