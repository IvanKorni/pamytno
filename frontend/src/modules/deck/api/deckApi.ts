import { json, request } from '@/shared'
import type { CardChanges, Flashcard, GenerationJob, Question, QuestionDecision } from '../model/types'

/** Вопросы темы в порядке появления. */
export const listQuestions = (topicId: string) => request<Question[]>(`/topics/${topicId}/questions`)

/** Запускает генерацию вопросов по единому тексту темы. */
export const generateQuestions = (topicId: string) =>
  request<GenerationJob>(`/topics/${topicId}/questions/generate`, { method: 'POST' })

/** Статус задачи генерации — для опроса. */
export const getGenerationJob = (jobId: string) => request<GenerationJob>(`/generation-jobs/${jobId}`)

/** Одобряет или отклоняет один вопрос. */
export const decideQuestion = (questionId: string, decision: QuestionDecision) =>
  request<Question>(`/questions/${questionId}/${decision === 'APPROVE' ? 'approve' : 'reject'}`, { method: 'POST' })

/** Одобряет или отклоняет несколько вопросов разом. */
export const decideQuestions = (questionIds: string[], decision: QuestionDecision) =>
  request<Question[]>('/questions/decisions', json('POST', { questionIds, decision }))

/** Запускает генерацию карточек по одобренным вопросам. */
export const generateCards = (topicId: string) =>
  request<GenerationJob>(`/topics/${topicId}/cards/generate`, { method: 'POST' })

/** Карточки темы в порядке создания. */
export const listCards = (topicId: string) => request<Flashcard[]>(`/topics/${topicId}/cards`)

/** Меняет вопрос или ответ карточки. */
export const updateCard = (cardId: string, changes: CardChanges) =>
  request<Flashcard>(`/cards/${cardId}`, json('PATCH', changes))

/** Удаляет карточку. */
export const deleteCard = (cardId: string) => request<void>(`/cards/${cardId}`, { method: 'DELETE' })
