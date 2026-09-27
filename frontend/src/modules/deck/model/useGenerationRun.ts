import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useGenerationJob } from '../api/questionQueries'
import { jobFailure, jobStorage, type JobKind } from './generationJobs'
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
  const polling = Boolean(jobId) && !job.isError && job.data?.status !== 'READY' && job.data?.status !== 'ERROR'
  return {
    launch: () => launch.mutate(),
    running: launch.isPending || polling,
    itemsCreated: job.data?.itemsCreated,
    readyJob: job.data?.status === 'READY' ? job.data : undefined,
    error: launch.error?.message ?? jobFailure(job.data, failureText) ?? job.error?.message,
  }
}
