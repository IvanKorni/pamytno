import { useQuery } from '@tanstack/react-query'
import { getGenerationJob, listQuestions } from './deckApi'
import { deckKeys } from './deckKeys'

/** Как часто опрашивать задачу генерации, мс. */
const JOB_POLL_INTERVAL_MS = 1200

/** Вопросы темы. */
export function useQuestions(topicId: string) {
  return useQuery({ queryKey: deckKeys.questions(topicId), queryFn: () => listQuestions(topicId) })
}

/** Задача генерации; опрашивается, пока известен её идентификатор. */
export function useGenerationJob(jobId?: string) {
  return useQuery({
    queryKey: deckKeys.job(jobId),
    queryFn: () => getGenerationJob(jobId!),
    enabled: Boolean(jobId),
    refetchInterval: JOB_POLL_INTERVAL_MS,
  })
}
