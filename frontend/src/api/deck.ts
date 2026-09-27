import { json, request } from './client'
import type { Flashcard, GenerationJob, Question, QuestionStatus } from '../types'

export const listQuestions = (topicId: string, status?: QuestionStatus) => request<Question[]>(`/topics/${topicId}/questions${status ? `?status=${status}` : ''}`)
export const generateQuestions = (topicId: string) => request<GenerationJob>(`/topics/${topicId}/questions/generate`, { method: 'POST' })
export const getGenerationJob = (id: string) => request<GenerationJob>(`/generation-jobs/${id}`)
export const updateQuestion = (id: string, text: string) => request<Question>(`/questions/${id}`, json('PATCH', { text }))
export const decideQuestion = (id: string, decision: 'APPROVE' | 'REJECT') => request<Question>(`/questions/${id}/${decision === 'APPROVE' ? 'approve' : 'reject'}`, { method: 'POST' })
export const decideQuestions = (questionIds: string[], decision: 'APPROVE' | 'REJECT') => request<Question[]>('/questions/decisions', json('POST', { questionIds, decision }))
export const generateCards = (topicId: string) => request<GenerationJob>(`/topics/${topicId}/cards/generate`, { method: 'POST' })
export const listCards = (topicId: string) => request<Flashcard[]>(`/topics/${topicId}/cards`)
export const updateCard = (id: string, body: { front?: string; back?: string }) => request<Flashcard>(`/cards/${id}`, json('PATCH', body))
export const deleteCard = (id: string) => request<void>(`/cards/${id}`, { method: 'DELETE' })

