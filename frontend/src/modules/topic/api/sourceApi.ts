import { json, request } from '@/shared'
import type { Source, TextSourceType, TopicContent } from '../model/types'

/** Текст или список слов для добавления в тему. */
export interface TextSourceBody {
  type: TextSourceType
  name: string
  text: string
}

/** Ссылка на видео YouTube для добавления в тему. */
export interface YoutubeSourceBody {
  url: string
  name?: string
}

/** Источники темы в порядке добавления. */
export const listSources = (topicId: string) => request<Source[]>(`/topics/${topicId}/sources`)

/** Актуальный единый текст темы. */
export const getTopicContent = (topicId: string) => request<TopicContent>(`/topics/${topicId}/content`)

/** Удаляет источник; единый текст пересобирается без него. */
export const deleteSource = (sourceId: string) => request<void>(`/sources/${sourceId}`, { method: 'DELETE' })

/** Добавляет текст или список слов; обработка идёт асинхронно. */
export const addTextSource = (topicId: string, body: TextSourceBody) =>
  request<Source>(`/topics/${topicId}/sources/text`, json('POST', body))

/** Добавляет видео YouTube; субтитры backend получает асинхронно. */
export const addYoutubeSource = (topicId: string, body: YoutubeSourceBody) =>
  request<Source>(`/topics/${topicId}/sources/youtube`, json('POST', body))

/** Загружает PDF; текст извлекается асинхронно. */
export function addPdfSource(topicId: string, file: File, name?: string) {
  const body = new FormData()
  body.append('file', file)
  if (name) body.append('name', name)
  return request<Source>(`/topics/${topicId}/sources/pdf`, { method: 'POST', body })
}
