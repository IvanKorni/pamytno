import { useEffect, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { completeLearningSession, getDueCards, reviewCard, startLearningSession } from '../api/learningApi'
import { learningKeys } from '../api/learningKeys'
import { useLearning } from './learningStore'
import type { DueCard } from './types'

/** Сценарий учебной сессии: старт, ответы «Помню» / «Не помню» и завершение. */
export function useLearningFlow(topicId: string) {
  const queryClient = useQueryClient()
  const { session, queue, remembered, forgotten, setSession, record, reset } = useLearning()
  const [ready, setReady] = useState(false)
  const [revealed, setRevealed] = useState(false)
  const [finished, setFinished] = useState(false)
  const [finalSession, setFinalSession] = useState(session)
  const invalidateProgress = () => {
    queryClient.invalidateQueries({ queryKey: learningKeys.progress(topicId) })
    queryClient.invalidateQueries({ queryKey: learningKeys.dashboard })
  }
  const start = useMutation({
    mutationFn: () => startLearningSession(topicId),
    onSuccess: async (newSession) => {
      const cards = await queryClient.fetchQuery({
        queryKey: learningKeys.dueCards(topicId),
        queryFn: () => getDueCards(topicId),
      })
      setSession(newSession, cards)
      setReady(true)
    },
    onError: () => setReady(true),
  })
  const review = useMutation({
    mutationFn: ({ card, result }: { card: DueCard; result: 'REMEMBER' | 'FORGOT' }) =>
      reviewCard(card.cardId, result, session?.id),
    onSuccess: (outcome, variables) => {
      record(outcome.result === 'REMEMBER', outcome.returnToSession ? variables.card : undefined)
      setRevealed(false)
      invalidateProgress()
    },
  })
  const complete = useMutation({
    mutationFn: () => completeLearningSession(session!.id),
    onSuccess: (result) => {
      setFinalSession(result)
      setFinished(true)
      invalidateProgress()
    },
  })
  useEffect(() => {
    if (session && session.topicId !== topicId) {
      reset()
      setReady(false)
      setFinished(false)
    }
  }, [topicId, session?.topicId])
  useEffect(() => {
    if (!session && !start.isPending && !ready) start.mutate()
  }, [session, ready])
  useEffect(() => {
    if (session && ready && !queue.length && !complete.isPending && !finished) complete.mutate()
  }, [queue.length, ready, session, finished])
  return {
    loading: !ready || start.isPending,
    finished: finished || complete.isSuccess,
    session,
    finalSession,
    card: queue[0],
    queueLength: queue.length,
    remembered,
    forgotten,
    revealed,
    reveal: () => setRevealed(true),
    answer: (result: 'REMEMBER' | 'FORGOT') => queue[0] && review.mutate({ card: queue[0], result }),
    answering: review.isPending,
    reset,
  }
}
