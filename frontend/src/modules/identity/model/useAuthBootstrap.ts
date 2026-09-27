import { useEffect, useState } from 'react'
import { tokenStorage, UNAUTHORIZED_EVENT } from '@/shared'
import { getCurrentUser } from '../api/identityApi'
import { useAuth } from './authStore'
import { useLogout } from './useLogout'

/**
 * Восстанавливает пользователя по сохранённому токену при старте приложения
 * и разлогинивает его, когда backend отвечает 401.
 *
 * @return `true`, пока профиль загружается
 */
export function useAuthBootstrap(): boolean {
  const [booting, setBooting] = useState(() => Boolean(tokenStorage.get()))
  const logout = useLogout()
  useEffect(() => {
    const onUnauthorized = () => {
      logout('expired')
      setBooting(false)
    }
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [logout])
  useEffect(() => {
    if (!tokenStorage.get()) return
    getCurrentUser()
      .then(useAuth.getState().setUser)
      .catch(() => logout('expired'))
      .finally(() => setBooting(false))
  }, [logout])
  return booting
}
