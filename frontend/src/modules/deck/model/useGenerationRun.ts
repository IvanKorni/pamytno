import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useGenerationJob } from '../api/questionQueries'
import { isJobGone, isJobPolling, jobFailure, jobStorage, type JobKind } from './generationJobs'
import type { GenerationJob } from './types'

/** Что нужно для запуска и отслеживания генерации; `T` — данные запуска, например текст для карточек слов. */
export interface GenerationRunOptions<T> {
  topicId: string
  kind: JobKind
  start: (input: T) => Promise<GenerationJob>
  onReady: (job: GenerationJob) => void
  failureText: string
}

/**
 * Запуск генерации и опрос её задачи до `READY` или `ERROR`. Идущая задача переживает уход с экрана
 * и перезагрузку; упавшая задача показывает ошибку и не держит экран в состоянии «генерируем».
 */
export function useGenerationRun<T = void>({ topicId, kind, start, onReady, failureText }: GenerationRunOptions<T>) {
  const [jobId, setJobId] = useState(() => jobStorage.get(topicId, kind))
  const job = useGenerationJob(jobId, {
    onFinished: (finished) => {
      jobStorage.clear(topicId, kind)
      if (finished.status === 'READY') onReady(finished)
    },
    onLost: () => jobStorage.clear(topicId, kind),
  })
  const launch = useMutation({
    mutationFn: start,
    onSuccess: (created) => {
      jobStorage.save(topicId, kind, created.id)
      setJobId(created.id)
    },
  })
  const current = job.data
  const launchError = launch.error?.message
  const jobError = jobFailure(current, failureText) ?? lostMessage(job.error)
  return {
    launch: (input: T) => launch.mutate(input),
    /** Запускает генерацию и, когда backend принял задачу, выполняет `onStarted` — например, переход на экран. */
    launchThen: (onStarted: () => void, input: T) => launch.mutate(input, { onSuccess: () => onStarted() }),
    /** Backend не принял задачу — ошибка запуска без ошибок прошлых задач. */
    launchError,
    /** Забывает ошибку прошлого запуска — например, когда окно запуска открывают заново. */
    resetLaunch: () => launch.reset(),
    /** Задача упала или потерялась на backend — без ошибки запуска, которую показывает окно запуска. */
    jobError,
    running: launch.isPending || isJobPolling({ jobId, job: current, lost: isJobGone(job.error) }),
    itemsCreated: current?.itemsCreated,
    readyJob: current?.status === 'READY' ? current : undefined,
    error: launchError ?? jobError,
  }
}

/** Сообщение о потерянной задаче; временные ошибки опроса не показываются — опрос продолжается. */
function lostMessage(error: Error | null): string | undefined {
  return isJobGone(error) ? error?.message : undefined
}
