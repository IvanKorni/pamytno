import { json, request } from './client'
import type { Source, SourceText, TopicContent } from '../types'

export const listSources = (topicId: string) => request<Source[]>(`/topics/${topicId}/sources`)
export const getSource = (id: string) => request<Source>(`/sources/${id}`)
export const getSourceText = (id: string) => request<SourceText>(`/sources/${id}/text`)
export const getTopicContent = (topicId: string) => request<TopicContent>(`/topics/${topicId}/content`)
export const deleteSource = (id: string) => request<void>(`/sources/${id}`, { method: 'DELETE' })
export const addTextSource = (topicId: string, body: { type: 'TEXT' | 'WORD_LIST'; name: string; text: string }) => request<Source>(`/topics/${topicId}/sources/text`, json('POST', body))
export const addYoutubeSource = (topicId: string, body: { url: string; name?: string }) => request<Source>(`/topics/${topicId}/sources/youtube`, json('POST', body))
export const addPdfSource = (topicId: string, file: File, name?: string) => {
  const body = new FormData(); body.append('file', file); if (name) body.append('name', name)
  return request<Source>(`/topics/${topicId}/sources/pdf`, { method: 'POST', body })
}

