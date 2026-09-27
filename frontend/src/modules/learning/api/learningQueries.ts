import { useQuery } from '@tanstack/react-query'
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
