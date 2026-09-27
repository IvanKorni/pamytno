import { useEffect, useReducer, useRef } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useToast } from '@/shared'
import { completeLearningSession, getDueCards, reviewCard, startLearningSession } from '../api/learningApi'
import { useRefreshProgress } from '../api/learningQueries'
import {
  INITIAL_LEARNING_STATE,
  learningReducer,
  nextQueue,
  type LearningAction,
  type LearningState,
} from './learningSession'
import type { DueCard, LearningSession } from './types'

/** Ответ пользователя вместе с состоянием сессии на момент ответа. */
interface Answer {
  card: DueCard
  result: 'REMEMBER' | 'FORGOT'
  session: LearningSession
  queue: DueCard[]
}

/**
 * Учебная сессия по теме. Карточки к повторению каждый раз берутся с backend, а не из кеша:
 * иначе можно повторно оценить уже пройденную карточку. Если повторять нечего, сессия не создаётся.
 * Состояние живёт, пока открыт экран: уход с экрана и возврат начинают новую сессию.
 */
export function useLearningSession(topicId: string) {
  const [state, dispatch] = useReducer(learningReducer, INITIAL_LEARNING_STATE)
  const started = useRef(false)
  const invalidateProgress = useRefreshProgress(topicId)
  const complete = useCompleteSession(invalidateProgress)
  useCompleteOnLeave(state)
  const review = useMutation({
    mutationFn: (answer: Answer) => reviewCard(answer.card.cardId, answer.result, answer.session.id),
    onSuccess: (outcome, answer) => {
      dispatch({ type: 'answered', outcome, card: answer.card })
      invalidateProgress()
      if (!nextQueue(answer.queue, outcome, answer.card).length) complete.mutate(answer.session.id)
    },
  })
  useEffect(() => {
    if (started.current) return
    started.current = true
    beginSession(topicId).then(dispatch)
  }, [topicId])
  return {
    state,
    reveal: () => dispatch({ type: 'revealed' }),
    answer: (result: Answer['result']) => {
      if (state.phase !== 'active') return
      review.mutate({ card: state.queue[0], result, session: state.session, queue: state.queue })
    },
    answering: review.isPending,
    answerError: review.error?.message,
    retry: () => {
      dispatch({ type: 'restarted' })
      beginSession(topicId).then(dispatch)
    },
  }
}

/**
 * Завершает начатую сессию, если с экрана ушли, не ответив на все карточки, — иначе на backend
 * копились бы незакрытые сессии. Итоги уже не показать, поэтому ошибка не важна.
 */
function useCompleteOnLeave(state: LearningState) {
  const openSession = useRef<string | undefined>(undefined)
  useEffect(() => {
    openSession.current = state.phase === 'active' ? state.session.id : undefined
  })
  useEffect(
    () => () => {
      if (openSession.current) completeLearningSession(openSession.current).catch(() => undefined)
    },
    [],
  )
}

/** Загружает карточки к повторению и, если они есть, начинает сессию на backend. */
async function beginSession(topicId: string): Promise<LearningAction> {
  try {
    const cards = await getDueCards(topicId)
    if (!cards.length) return { type: 'empty' }
    return { type: 'started', session: await startLearningSession(topicId), cards }
  } catch (error) {
    return { type: 'failed', message: (error as Error).message }
  }
}

/** Завершение сессии на backend; итоги уже на экране, поэтому ошибка только показывается уведомлением. */
function useCompleteSession(onSettled: () => void) {
  const showToast = useToast((toast) => toast.show)
  return useMutation({
    mutationFn: completeLearningSession,
    onError: () => showToast('Не удалось сохранить итоги сессии', 'error'),
    onSettled,
  })
}
