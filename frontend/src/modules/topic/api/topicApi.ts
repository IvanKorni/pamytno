import { json, request } from '@/shared'
import type { Topic } from '../model/types'

/** Изменяемые поля темы. */
export interface TopicChanges {
  title?: string
  description?: string
}

/** Темы текущего пользователя, новые сверху. */
export const listTopics = () => request<Topic[]>('/topics')

/** Тема по идентификатору. */
export const getTopic = (topicId: string) => request<Topic>(`/topics/${topicId}`)

/** Создаёт тему; пустое описание не отправляется. */
export const createTopic = (title: string, description?: string) =>
  request<Topic>('/topics', json('POST', { title, description: description || undefined }))

/** Меняет название или описание темы. */
export const updateTopic = (topicId: string, changes: TopicChanges) =>
  request<Topic>(`/topics/${topicId}`, json('PATCH', changes))

/** Удаляет тему со всеми материалами. */
export const deleteTopic = (topicId: string) => request<void>(`/topics/${topicId}`, { method: 'DELETE' })
