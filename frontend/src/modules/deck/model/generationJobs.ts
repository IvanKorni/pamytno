import { sessionValues } from '@/shared'
import type { GenerationJob } from './types'

/** Вид задачи генерации: вопросы или карточки. */
export type JobKind = GenerationJob['type']

/** Как часто опрашивать задачу, пока она выполняется, мс. */
export const JOB_POLL_INTERVAL_MS = 1200

/** Ключ, под которым запоминается идущая задача темы. */
const storageKey = (topicId: string, kind: JobKind) => `pamytno-job:${topicId}:${kind}`

/**
 * Идущие задачи генерации по темам. Запоминаются, чтобы после ухода с экрана или перезагрузки
 * продолжить опрос, а не запускать генерацию заново (backend ответит `GENERATION_IN_PROGRESS`).
 */
export const jobStorage = {
  /** Идущая задача темы, если она есть. */
  get: (topicId: string, kind: JobKind) => sessionValues.get(storageKey(topicId, kind)),
  /** Запоминает запущенную задачу. */
  save: (topicId: string, kind: JobKind, jobId: string) => sessionValues.set(storageKey(topicId, kind), jobId),
  /** Забывает задачу, когда она завершилась. */
  clear: (topicId: string, kind: JobKind) => sessionValues.remove(storageKey(topicId, kind)),
}

/** Интервал опроса: пока задача не завершилась — опрашиваем, после `READY` или `ERROR` — нет. */
export function jobPollInterval(job?: GenerationJob): number | false {
  return !job || job.status === 'PROCESSING' ? JOB_POLL_INTERVAL_MS : false
}

/** Сообщение об ошибке завершившейся задачи или `undefined`, если задача не упала. */
export function jobFailure(job: GenerationJob | undefined, fallback: string): string | undefined {
  return job?.status === 'ERROR' ? job.errorMessage || fallback : undefined
}
