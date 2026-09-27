import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { generateCards, generateQuestions } from '../api/deckApi'
import { deckKeys } from '../api/deckKeys'
import { useDecideQuestion, useDecideQuestions, useQuestions } from '../api/questionQueries'
import type { QuestionDecision } from './types'
import { useGenerationRun } from './useGenerationRun'

/**
 * Сценарий отбора вопросов: генерация вопросов, решения по одному и списком, генерация карточек.
 *
 * @param topicId        тема
 * @param onCardsCreated что перечитать в других модулях, когда появились новые карточки
 */
export function useQuestionsWorkflow(topicId: string, onCardsCreated: () => void) {
  const queryClient = useQueryClient()
  const [selected, setSelected] = useState<string[]>([])
  const questions = useQuestions(topicId)
  const decideOne = useDecideQuestion(topicId)
  const decideMany = useDecideQuestions(topicId, () => setSelected([]))
  const refetch = (queryKey: readonly unknown[]) => queryClient.invalidateQueries({ queryKey })
  const questionRun = useGenerationRun({
    topicId,
    kind: 'QUESTIONS',
    start: () => generateQuestions(topicId),
    onReady: () => refetch(deckKeys.questions(topicId)),
    failureText: 'Не удалось создать вопросы.',
  })
  const cardRun = useGenerationRun({
    topicId,
    kind: 'CARDS',
    start: async () => {
      if (selected.length) await decideMany.mutateAsync({ ids: selected, decision: 'APPROVE' })
      return generateCards(topicId)
    },
    onReady: () => {
      refetch(deckKeys.questions(topicId))
      refetch(deckKeys.cards(topicId))
      onCardsCreated()
    },
    failureText: 'Не удалось создать карточки.',
  })
  return {
    questions,
    selected,
    setSelected,
    questionRun,
    cardRun,
    decide: (id: string, decision: QuestionDecision) => decideOne.mutate({ id, decision }),
    deciding: decideOne.isPending || decideMany.isPending,
    rejectSelected: () => decideMany.mutate({ ids: selected, decision: 'REJECT' }),
    error: questionRun.error ?? cardRun.error ?? decideOne.error?.message ?? decideMany.error?.message,
  }
}

/** Сценарий отбора вопросов, который возвращает `useQuestionsWorkflow`. */
export type QuestionsWorkflow = ReturnType<typeof useQuestionsWorkflow>
