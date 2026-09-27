import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getDashboard, getTopicProgress } from './learningApi'
import { learningKeys } from './learningKeys'

/** Общий прогресс пользователя по всем темам. */
export function useDashboard() {
  return useQuery({ queryKey: learningKeys.dashboard, queryFn: getDashboard })
}

/** Прогресс изучения темы. */
export function useTopicProgress(topicId: string) {
  return useQuery({ queryKey: learningKeys.progress(topicId), queryFn: () => getTopicProgress(topicId) })
}

/**
 * Функция, которая перечитывает прогресс темы и dashboard, — для всего, что меняет карточки темы:
 * ответы в обучении, создание и удаление карточек.
 */
export function useRefreshProgress(topicId: string): () => void {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: learningKeys.progress(topicId) })
    queryClient.invalidateQueries({ queryKey: learningKeys.dashboard })
  }
}
