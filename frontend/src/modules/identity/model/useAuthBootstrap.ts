import { useCallback, useEffect, useState } from 'react'
import { HttpError, tokenStorage, UNAUTHORIZED_EVENT } from '@/shared'
import { getCurrentUser } from '../api/identityApi'
import { useAuth } from './authStore'
import { useLogout } from './useLogout'

/** Состояние восстановления входа при старте. */
export type BootStatus = 'booting' | 'ready' | 'failed'

/**
 * Восстанавливает пользователя по сохранённому токену при старте и разлогинивает его, когда backend
 * отвечает 401. Сетевая ошибка или 5xx токен не удаляют — приложение предлагает повторить.
 */
export function useAuthBootstrap(): { status: BootStatus; retry: () => void } {
  const [status, setStatus] = useState<BootStatus>(() => (tokenStorage.get() ? 'booting' : 'ready'))
  const logout = useLogout()
  useEffect(() => {
    const onUnauthorized = () => {
      logout('expired')
      setStatus('ready')
    }
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [logout])
  const restore = useCallback(() => {
    getCurrentUser()
      .then((user) => {
        useAuth.getState().setUser(user)
        setStatus('ready')
      })
      .catch((error: unknown) => setStatus(isUnauthorized(error) ? 'ready' : 'failed'))
  }, [])
  useEffect(() => {
    if (tokenStorage.get()) restore()
  }, [restore])
  const retry = () => {
    setStatus('booting')
    restore()
  }
  return { status, retry }
}

/** Backend отверг токен — выход уже выполнен по событию 401. */
function isUnauthorized(error: unknown): boolean {
  return error instanceof HttpError && error.status === 401
}
