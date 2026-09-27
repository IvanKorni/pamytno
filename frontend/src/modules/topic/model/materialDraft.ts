import { addPdfSource, addTextSource, addYoutubeSource } from '../api/sourceApi'
import type { Source, SourceType } from './types'

/** Черновик материала в форме добавления; название общее для всех типов. */
export interface MaterialDraft {
  kind: SourceType
  name: string
  text: string
  url: string
  file?: File
}

/** Пустой черновик: по умолчанию добавляется текст. */
export const EMPTY_DRAFT: MaterialDraft = { kind: 'TEXT', name: '', text: '', url: '' }

/** Проверяет, что в черновике заполнено главное поле выбранного типа. */
export function isDraftReady(draft: MaterialDraft): boolean {
  if (draft.kind === 'PDF') return Boolean(draft.file)
  if (draft.kind === 'YOUTUBE') return Boolean(draft.url)
  return Boolean(draft.text.trim())
}

/** Отправляет черновик в нужный эндпоинт по его типу. */
export function saveMaterial(topicId: string, draft: MaterialDraft): Promise<Source> {
  if (draft.kind === 'PDF') {
    return draft.file ? addPdfSource(topicId, draft.file, draft.name) : Promise.reject(new Error('Выберите PDF-файл'))
  }
  if (draft.kind === 'YOUTUBE') return addYoutubeSource(topicId, { url: draft.url, name: draft.name || undefined })
  const defaultName = draft.kind === 'WORD_LIST' ? 'Список слов' : 'Мои заметки'
  return addTextSource(topicId, { type: draft.kind, name: draft.name || defaultName, text: draft.text })
}
