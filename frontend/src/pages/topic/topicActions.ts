import { useOutletContext } from 'react-router-dom'

/** Действия экрана темы, общие для всех вкладок: окно добавления материала живёт в шапке темы. */
export interface TopicActions {
  addMaterial: () => void
}

/** Действия экрана темы, доступные вкладке. */
export function useTopicActions(): TopicActions {
  return useOutletContext<TopicActions>()
}
