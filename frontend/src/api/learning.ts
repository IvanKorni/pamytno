import { json, request } from './client'
import type { Dashboard, DueCard, LearningSession, ReviewOutcome, ReviewResult, TopicProgress } from '../types'

export const getDashboard = () => request<Dashboard>('/dashboard')
export const getTopicProgress = (topicId: string) => request<TopicProgress>(`/topics/${topicId}/progress`)
export const getDueCards = (topicId: string) => request<DueCard[]>(`/topics/${topicId}/reviews/due?limit=500`)
export const startLearningSession = (topicId: string) => request<LearningSession>(`/topics/${topicId}/learning-sessions`, { method: 'POST' })
export const getLearningSession = (id: string) => request<LearningSession>(`/learning-sessions/${id}`)
export const completeLearningSession = (id: string) => request<LearningSession>(`/learning-sessions/${id}/complete`, { method: 'POST' })
export const reviewCard = (cardId: string, result: ReviewResult, sessionId?: string) => request<ReviewOutcome>(`/cards/${cardId}/review`, json('POST', { result, sessionId }))

