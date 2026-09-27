import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { isJobGone, jobPollInterval } from '../model/generationJobs'
import type { GenerationJob, Question, QuestionDecision } from '../model/types'
import { decideQuestion, decideQuestions, getGenerationJob, listQuestions } from './deckApi'
import { deckKeys } from './deckKeys'

/** Вопросы темы. */
export function useQuestions(topicId: string) {
  return useQuery({ queryKey: deckKeys.questions(topicId), queryFn: () => listQuestions(topicId) })
}

/** Реакция на завершение задачи генерации. */
export interface JobListener {
  /** Задача впервые пришла завершённой (`READY` или `ERROR`). */
  onFinished: (job: GenerationJob) => void
  /** Задачи больше нет на backend (404); при сетевой ошибке или 5xx опрос просто продолжается. */
  onLost: () => void
}

/**
 * Задача генерации: опрашивается, пока выполняется, в том числе после временной ошибки сети или backend.
 * Реакция вызывается из загрузки — завершённая задача больше не перезапрашивается, поэтому и реакция
 * срабатывает один раз.
 */
export function useGenerationJob(jobId: string | undefined, listener: JobListener) {
  return useQuery({
    queryKey: deckKeys.job(jobId),
    queryFn: async () => {
      const job = await getGenerationJob(jobId!).catch((error: unknown) => {
        if (isJobGone(error)) listener.onLost()
        throw error
      })
      if (job.status !== 'PROCESSING') listener.onFinished(job)
      return job
    },
    enabled: Boolean(jobId),
    refetchInterval: (query) => jobPollInterval(query.state.data),
    staleTime: (query) => (query.state.data?.status === 'PROCESSING' ? 0 : Infinity),
  })
}

/** Решение по одному вопросу; ответ backend сразу подменяет вопрос в списке, без ожидания перезагрузки. */
export function useDecideQuestion(topicId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, decision }: { id: string; decision: QuestionDecision }) => decideQuestion(id, decision),
    onSuccess: (updated) => replaceQuestions(queryClient, topicId, [updated]),
  })
}

/** Решение по нескольким вопросам разом. */
export function useDecideQuestions(topicId: string, onDecided: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ ids, decision }: { ids: string[]; decision: QuestionDecision }) => decideQuestions(ids, decision),
    onSuccess: (updated) => {
      replaceQuestions(queryClient, topicId, updated)
      onDecided()
    },
  })
}

/** Подменяет изменённые вопросы в кеше списка вопросов темы. */
function replaceQuestions(queryClient: ReturnType<typeof useQueryClient>, topicId: string, updated: Question[]) {
  const byId = new Map(updated.map((question) => [question.id, question]))
  queryClient.setQueryData<Question[]>(deckKeys.questions(topicId), (list) =>
    list?.map((question) => byId.get(question.id) ?? question),
  )
}
