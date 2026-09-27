import { useCallback } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuth, type LogoutReason } from './authStore'

/**
 * Выход: забывает токен и пользователя и очищает кеш запросов — иначе следующий пользователь
 * в этом браузере увидел бы из кеша темы и прогресс предыдущего.
 */
export function useLogout(): (reason: LogoutReason) => void {
  const queryClient = useQueryClient()
  const logout = useAuth((state) => state.logout)
  return useCallback(
    (reason: LogoutReason) => {
      logout(reason)
      queryClient.clear()
    },
    [logout, queryClient],
  )
}
