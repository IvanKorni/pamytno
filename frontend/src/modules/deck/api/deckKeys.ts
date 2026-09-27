/** Ключи кеша TanStack Query для данных модуля deck. */
export const deckKeys = {
  /** Вопросы темы. */
  questions: (topicId: string) => ['questions', topicId] as const,
  /** Задача генерации. */
  job: (jobId?: string) => ['job', jobId] as const,
  /** Карточки темы. */
  cards: (topicId: string) => ['cards', topicId] as const,
}
