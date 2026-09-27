import { json, request } from '@/shared'
import type { Dashboard, DueCard, LearningSession, ReviewOutcome, ReviewResult, TopicProgress } from '../model/types'

/** Максимум карточек, которые backend отдаёт к повторению за раз. */
const DUE_CARDS_LIMIT = 500

/** Общий прогресс пользователя по всем темам. */
export const getDashboard = () => request<Dashboard>('/dashboard')

/** Прогресс изучения темы. */
export const getTopicProgress = (topicId: string) => request<TopicProgress>(`/topics/${topicId}/progress`)

/** Карточки темы, срок повторения которых наступил, — самые давние первыми. */
export const getDueCards = (topicId: string) =>
  request<DueCard[]>(`/topics/${topicId}/reviews/due?limit=${DUE_CARDS_LIMIT}`)

/** Начинает учебную сессию по теме. */
export const startLearningSession = (topicId: string) =>
  request<LearningSession>(`/topics/${topicId}/learning-sessions`, { method: 'POST' })

/** Завершает учебную сессию и возвращает её итоги. */
export const completeLearningSession = (sessionId: string) =>
  request<LearningSession>(`/learning-sessions/${sessionId}/complete`, { method: 'POST' })

/** Отправляет ответ по карточке; новое расписание считает backend. */
export const reviewCard = (cardId: string, result: ReviewResult, sessionId?: string) =>
  request<ReviewOutcome>(`/cards/${cardId}/review`, json('POST', { result, sessionId }))
