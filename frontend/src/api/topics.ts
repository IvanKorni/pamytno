import { json, request } from './client'
import type { Topic } from '../types'

export const listTopics = () => request<Topic[]>('/topics')
export const getTopic = (id: string) => request<Topic>(`/topics/${id}`)
export const createTopic = (title: string, description?: string) => request<Topic>('/topics', json('POST', { title, description: description || undefined }))
export const updateTopic = (id: string, body: { title?: string; description?: string }) => request<Topic>(`/topics/${id}`, json('PATCH', body))
export const deleteTopic = (id: string) => request<void>(`/topics/${id}`, { method: 'DELETE' })

