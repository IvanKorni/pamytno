/** Ключи кеша TanStack Query для данных модуля learning. */
export const learningKeys = {
  /** Общий прогресс по всем темам. */
  dashboard: ['dashboard'] as const,
  /** Прогресс темы. */
  progress: (topicId: string) => ['progress', topicId] as const,
}
