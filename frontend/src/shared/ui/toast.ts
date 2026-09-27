import { create } from 'zustand'

/** Тон уведомления. */
export type ToastTone = 'success' | 'error'

/** Состояние всплывающего уведомления. */
interface ToastState {
  toast?: { message: string; tone: ToastTone }
  show: (message: string, tone?: ToastTone) => void
  clear: () => void
}

/** Всплывающее уведомление: показывается одно за раз и скрывается само. */
export const useToast = create<ToastState>((set) => ({
  show: (message, tone = 'success') => set({ toast: { message, tone } }),
  clear: () => set({ toast: undefined }),
}))
