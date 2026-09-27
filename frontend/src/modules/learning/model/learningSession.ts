import type { DueCard, LearningSession, ReviewOutcome } from './types'

/** Ответы пользователя за сессию. */
interface Answers {
  remembered: number
  forgotten: number
}

/** Идущая или завершённая сессия: очередь карточек и ответы. */
export interface SessionProgress extends Answers {
  phase: 'active' | 'finished'
  session: LearningSession
  queue: DueCard[]
  revealed: boolean
}

/** Состояние экрана обучения. */
export type LearningState =
  | { phase: 'loading' }
  | { phase: 'empty' }
  | { phase: 'failed'; message: string }
  | SessionProgress

/** Событие экрана обучения. */
export type LearningAction =
  | { type: 'restarted' }
  | { type: 'started'; session: LearningSession; cards: DueCard[] }
  | { type: 'empty' }
  | { type: 'failed'; message: string }
  | { type: 'revealed' }
  | { type: 'answered'; outcome: ReviewOutcome; card: DueCard }

/** Экран обучения открывается с загрузки карточек. */
export const INITIAL_LEARNING_STATE: LearningState = { phase: 'loading' }

/** Переводит экран обучения в следующее состояние по событию. */
export function learningReducer(state: LearningState, action: LearningAction): LearningState {
  switch (action.type) {
    case 'restarted':
      return INITIAL_LEARNING_STATE
    case 'started':
      return { phase: 'active', session: action.session, queue: action.cards, revealed: false, remembered: 0, forgotten: 0 }
    case 'empty':
      return { phase: 'empty' }
    case 'failed':
      return { phase: 'failed', message: action.message }
    case 'revealed':
      return state.phase === 'active' ? { ...state, revealed: true } : state
    case 'answered':
      return state.phase === 'active' ? applyAnswer(state, action.outcome, action.card) : state
  }
}

/**
 * Очередь после ответа: карточка уходит из головы очереди, а забытую backend может вернуть
 * в хвост текущей сессии (`returnToSession`).
 */
export function nextQueue(queue: DueCard[], outcome: ReviewOutcome, card: DueCard): DueCard[] {
  const rest = queue[0]?.cardId === card.cardId ? queue.slice(1) : queue
  return outcome.returnToSession ? [...rest, card] : rest
}

/** Учитывает ответ: двигает очередь, считает ответы и завершает сессию на последней карточке. */
function applyAnswer(state: SessionProgress, outcome: ReviewOutcome, card: DueCard): SessionProgress {
  const queue = nextQueue(state.queue, outcome, card)
  const remembered = outcome.result === 'REMEMBER'
  return {
    ...state,
    phase: queue.length ? 'active' : 'finished',
    queue,
    revealed: false,
    remembered: state.remembered + (remembered ? 1 : 0),
    forgotten: state.forgotten + (remembered ? 0 : 1),
  }
}
