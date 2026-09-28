import type { SourceType, TopicStatus } from './types'

/** Подписи статусов темы. */
const TOPIC_STATUS_LABELS: Record<TopicStatus, string> = {
  DRAFT: 'Черновик',
  PROCESSING: 'Обрабатывается',
  READY: 'Готово',
  ERROR: 'Ошибка',
}

/** Подписи типов источников. */
const SOURCE_TYPE_LABELS: Record<SourceType, string> = {
  PDF: 'PDF-документ',
  YOUTUBE: 'Видео YouTube',
  TEXT: 'Текстовые заметки',
  WORD_LIST: 'Список слов',
}

/** Короткие подписи типов источников для колонки типа в списке материалов. */
const SOURCE_TYPE_SHORT: Record<SourceType, string> = {
  PDF: 'PDF',
  YOUTUBE: 'YouTube',
  TEXT: 'Текст',
  WORD_LIST: 'Слова',
}

/** Подпись статуса темы для пользователя. */
export const topicStatusLabel = (status: TopicStatus) => TOPIC_STATUS_LABELS[status]

/** Подпись типа источника для пользователя. */
export const sourceTypeLabel = (type: SourceType) => SOURCE_TYPE_LABELS[type]

/** Короткая подпись типа источника. */
export const sourceTypeShort = (type: SourceType) => SOURCE_TYPE_SHORT[type]
