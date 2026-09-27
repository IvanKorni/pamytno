import { useEffect, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { decideQuestion, decideQuestions, generateCards, generateQuestions } from '../api/deckApi'
import { deckKeys } from '../api/deckKeys'
import { useGenerationJob, useQuestions } from '../api/questionQueries'
import type { QuestionDecision } from './types'

/**
 * Сценарий отбора вопросов: генерация вопросов, решения по одному и списком, генерация карточек.
 *
 * @param topicId   тема
 * @param onChanged что перечитать в других модулях, когда вопросы или карточки изменились
 */
export function useQuestionsWorkflow(topicId: string, onChanged: () => void) {
  const queryClient = useQueryClient()
  const [selected, setSelected] = useState<string[]>([])
  const [jobId, setJobId] = useState<string>()
  const [cardJobId, setCardJobId] = useState<string>()
  const [error, setError] = useState('')
  const questions = useQuestions(topicId)
  const job = useGenerationJob(jobId)
  const cardJob = useGenerationJob(cardJobId)
  const onError = (e: Error) => setError(e.message)
  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: deckKeys.questions(topicId) })
    onChanged()
  }
  const generate = useMutation({
    mutationFn: () => generateQuestions(topicId),
    onSuccess: (data) => setJobId(data.id),
    onError,
  })
  const decide = useMutation({
    mutationFn: ({ id, decision }: { id: string; decision: QuestionDecision }) => decideQuestion(id, decision),
    onSuccess: refresh,
    onError,
  })
  const bulk = useMutation({
    mutationFn: (decision: QuestionDecision) => decideQuestions(selected, decision),
    onSuccess: () => {
      setSelected([])
      refresh()
    },
    onError,
  })
  const makeCards = useMutation({
    mutationFn: async () => {
      if (selected.length) await decideQuestions(selected, 'APPROVE')
      return generateCards(topicId)
    },
    onSuccess: (data) => {
      setSelected([])
      refresh()
      setCardJobId(data.id)
    },
    onError,
  })
  useEffect(() => {
    if (job.data?.status === 'READY') {
      setJobId(undefined)
      refresh()
    }
    if (job.data?.status === 'ERROR') setError(job.data.errorMessage || 'Не удалось создать вопросы.')
  }, [job.data?.status])
  useEffect(() => {
    if (cardJob.data?.status === 'READY') {
      setCardJobId(undefined)
      queryClient.invalidateQueries({ queryKey: deckKeys.cards(topicId) })
      onChanged()
    }
    if (cardJob.data?.status === 'ERROR') setError(cardJob.data.errorMessage || 'Не удалось создать карточки.')
  }, [cardJob.data?.status])
  return {
    questions,
    selected,
    setSelected,
    error,
    generate,
    decide,
    bulk,
    makeCards,
    job,
    cardJob,
    processingQuestions: generate.isPending || Boolean(jobId),
    processingCards: makeCards.isPending || Boolean(cardJobId),
  }
}
