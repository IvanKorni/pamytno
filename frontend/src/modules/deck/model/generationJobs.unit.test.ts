import { describe, expect, it } from 'vitest'
import { JOB_POLL_INTERVAL_MS, jobFailure, jobPollInterval, jobStorage } from './generationJobs'
import type { GenerationJob } from './types'

const job = (status: GenerationJob['status'], errorMessage?: string): GenerationJob => ({
  id: 'job-1',
  topicId: 't-1',
  type: 'QUESTIONS',
  status,
  itemsCreated: 0,
  createdAt: '2026-09-27T10:00:00Z',
  errorMessage,
})

describe('задачи генерации', () => {
  it('опрашивает задачу, пока она не завершилась', () => {
    expect(jobPollInterval(undefined)).toBe(JOB_POLL_INTERVAL_MS)
    expect(jobPollInterval(job('PROCESSING'))).toBe(JOB_POLL_INTERVAL_MS)
  })

  it('перестаёт опрашивать задачу после READY и ERROR', () => {
    expect(jobPollInterval(job('READY'))).toBe(false)
    expect(jobPollInterval(job('ERROR'))).toBe(false)
  })

  it('берёт сообщение об ошибке из задачи, а без него — запасное', () => {
    expect(jobFailure(job('ERROR', 'AI недоступен'), 'Не удалось')).toBe('AI недоступен')
    expect(jobFailure(job('ERROR'), 'Не удалось')).toBe('Не удалось')
    expect(jobFailure(job('READY'), 'Не удалось')).toBeUndefined()
  })

  it('запоминает идущую задачу отдельно по теме и виду', () => {
    // given
    jobStorage.save('t-1', 'QUESTIONS', 'job-1')

    // when / then
    expect(jobStorage.get('t-1', 'QUESTIONS')).toBe('job-1')
    expect(jobStorage.get('t-1', 'CARDS')).toBeUndefined()
    jobStorage.clear('t-1', 'QUESTIONS')
    expect(jobStorage.get('t-1', 'QUESTIONS')).toBeUndefined()
  })
})
