import { create } from 'zustand'
import type { DueCard, LearningSession } from './types'

/** Состояние текущей учебной сессии. */
interface LearningState {
  session?: LearningSession
  queue: DueCard[]
  remembered: number
  forgotten: number
  setSession: (session: LearningSession, cards: DueCard[]) => void
  record: (remembered: boolean, card?: DueCard) => void
  reset: () => void
}

/** Очередь карточек текущей учебной сессии и счётчики ответов. */
export const useLearning = create<LearningState>((set) => ({
  queue: [],
  remembered: 0,
  forgotten: 0,
  setSession: (session, cards) => set({ session, queue: cards, remembered: 0, forgotten: 0 }),
  record: (remembered, card) =>
    set((state) => ({
      remembered: state.remembered + (remembered ? 1 : 0),
      forgotten: state.forgotten + (remembered ? 0 : 1),
      queue: [...state.queue.slice(1), ...(card ? [card] : [])],
    })),
  reset: () => set({ session: undefined, queue: [], remembered: 0, forgotten: 0 }),
}))
