import { useEffect } from 'react'
import { Check } from 'lucide-react'
import { useToast } from './toast'

/** Время показа уведомления, мс. */
const TOAST_DURATION_MS = 3200

/** Показывает текущее уведомление и скрывает его по таймеру. */
export function ToastContainer() {
  const { toast, clear } = useToast()
  useEffect(() => {
    if (!toast) return undefined
    const timer = window.setTimeout(clear, TOAST_DURATION_MS)
    return () => window.clearTimeout(timer)
  }, [toast, clear])
  if (!toast) return null
  return (
    <div className={`toast toast-${toast.tone}`} role="status">
      <Check size={16} /> {toast.message}
    </div>
  )
}
