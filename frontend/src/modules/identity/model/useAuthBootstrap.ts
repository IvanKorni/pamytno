import { useEffect, useState } from 'react'
import { tokenStorage, UNAUTHORIZED_EVENT } from '@/shared'
import { getCurrentUser } from '../api/identityApi'
import { useAuth } from './authStore'

/**
 * Восстанавливает пользователя по сохранённому токену при старте приложения
 * и разлогинивает его, когда backend отвечает 401.
 *
 * @return `true`, пока профиль загружается
 */
export function useAuthBootstrap(): boolean {
  const [booting, setBooting] = useState(() => Boolean(tokenStorage.get()))
  useEffect(() => {
    const { setUser, logout } = useAuth.getState()
    const onUnauthorized = () => {
      logout()
      setBooting(false)
    }
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    if (tokenStorage.get()) {
      getCurrentUser()
        .then(setUser)
        .catch(logout)
        .finally(() => setBooting(false))
    }
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [])
  return booting
}
