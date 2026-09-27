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

/** Подпись статуса темы для пользователя. */
export const topicStatusLabel = (status: TopicStatus) => TOPIC_STATUS_LABELS[status]

/** Подпись типа источника для пользователя. */
export const sourceTypeLabel = (type: SourceType) => SOURCE_TYPE_LABELS[type]
