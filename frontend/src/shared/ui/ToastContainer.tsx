import { useEffect } from 'react'
import { useToast } from './toast'

/** Время показа уведомления, мс. */
const TOAST_DURATION_MS = 3200

/** Показывает текущее уведомление и скрывает его по таймеру; ошибки объявляются скринридеру сразу. */
export function ToastContainer() {
  const { toast, clear } = useToast()
  useEffect(() => {
    if (!toast) return undefined
    const timer = window.setTimeout(clear, TOAST_DURATION_MS)
    return () => window.clearTimeout(timer)
  }, [toast, clear])
  if (!toast) return null
  const isError = toast.tone === 'error'
  return (
    <div className={`toast toast-${toast.tone}`} role={isError ? 'alert' : 'status'}>
      {toast.message}
    </div>
  )
}
