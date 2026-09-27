import { useMutation } from '@tanstack/react-query'
import { tokenStorage } from '@/shared'
import { getCurrentUser, login, register } from '../api/identityApi'
import { useAuth } from './authStore'
import type { User } from './types'

/** Данные формы входа или регистрации. */
export interface Credentials {
  email: string
  password: string
}

/**
 * Вход или регистрация: сохраняет выданный токен и загружает профиль.
 *
 * @param mode      что делает форма — входит или регистрирует
 * @param onSuccess что сделать после успешного входа, например перейти на главную
 */
export function useSignIn(mode: 'login' | 'register', onSuccess: (user: User) => void) {
  const setUser = useAuth((state) => state.setUser)
  return useMutation({
    mutationFn: async ({ email, password }: Credentials) => {
      const token = mode === 'register' ? await register(email, password) : await login(email, password)
      tokenStorage.save(token.accessToken)
      return getCurrentUser()
    },
    onSuccess: (user) => {
      setUser(user)
      onSuccess(user)
    },
  })
}
