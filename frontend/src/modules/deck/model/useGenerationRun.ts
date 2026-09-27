import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useGenerationJob } from '../api/questionQueries'
import { isJobGone, isJobPolling, jobFailure, jobStorage, type JobKind } from './generationJobs'
import type { GenerationJob } from './types'

/** Что нужно для запуска и отслеживания генерации. */
export interface GenerationRunOptions {
  topicId: string
  kind: JobKind
  start: () => Promise<GenerationJob>
  onReady: () => void
  failureText: string
}

/**
 * Запуск генерации и опрос её задачи до `READY` или `ERROR`. Идущая задача переживает уход с экрана
 * и перезагрузку; упавшая задача показывает ошибку и не держит экран в состоянии «генерируем».
 */
export function useGenerationRun({ topicId, kind, start, onReady, failureText }: GenerationRunOptions) {
  const [jobId, setJobId] = useState(() => jobStorage.get(topicId, kind))
  const job = useGenerationJob(jobId, {
    onFinished: (finished) => {
      jobStorage.clear(topicId, kind)
      if (finished.status === 'READY') onReady()
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
  return {
    launch: () => launch.mutate(),
    running: launch.isPending || isJobPolling({ jobId, job: current, lost: isJobGone(job.error) }),
    itemsCreated: current?.itemsCreated,
    readyJob: current?.status === 'READY' ? current : undefined,
    error: launch.error?.message ?? jobFailure(current, failureText) ?? lostMessage(job.error),
  }
}

/** Сообщение о потерянной задаче; временные ошибки опроса не показываются — опрос продолжается. */
function lostMessage(error: Error | null): string | undefined {
  return isJobGone(error) ? error?.message : undefined
}
