/** Ключи кеша TanStack Query для данных модуля topic. */
export const topicKeys = {
  /** Список тем пользователя. */
  all: ['topics'] as const,
  /** Одна тема. */
  detail: (topicId: string) => ['topic', topicId] as const,
  /** Источники темы. */
  sources: (topicId: string) => ['sources', topicId] as const,
  /** Единый текст темы. */
  content: (topicId: string) => ['content', topicId] as const,
}
