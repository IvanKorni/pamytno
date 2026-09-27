import { create } from 'zustand'
import type { DueCard, LearningSession, User } from './types'

interface LearningState {
  session?: LearningSession
  queue: DueCard[]
  remembered: number
  forgotten: number
  setSession: (session: LearningSession, cards: DueCard[]) => void
  shiftCard: () => void
  record: (remembered: boolean, card?: DueCard) => void
  reset: () => void
}

export const useAuth = create<{ user?: User; setUser: (user?: User) => void; logout: () => void }>((set) => ({
  setUser: (user) => set({ user }),
  logout: () => { localStorage.removeItem('pamytno-token'); set({ user: undefined }) },
}))

export const useToast = create<{ toast?: { message: string; tone: 'success' | 'error' }; show: (message: string, tone?: 'success' | 'error') => void; clear: () => void }>((set) => ({
  show: (message, tone = 'success') => set({ toast: { message, tone } }),
  clear: () => set({ toast: undefined }),
}))

export const useLearning = create<LearningState>((set) => ({
  queue: [], remembered: 0, forgotten: 0,
  setSession: (session, cards) => set({ session, queue: cards, remembered: 0, forgotten: 0 }),
  shiftCard: () => set((state) => ({ queue: state.queue.slice(1) })),
  record: (remembered, card) => set((state) => ({
    remembered: state.remembered + (remembered ? 1 : 0),
    forgotten: state.forgotten + (remembered ? 0 : 1),
    queue: [...state.queue.slice(1), ...(card ? [card] : [])],
  })),
  reset: () => set({ session: undefined, queue: [], remembered: 0, forgotten: 0 }),
}))
