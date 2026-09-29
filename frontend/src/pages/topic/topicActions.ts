import { useOutletContext } from 'react-router-dom'
import type { VocabularyRun } from '@/modules/deck'

/**
 * Действия экрана темы, общие для всех вкладок: окна добавления материала и слов живут в шапке темы,
 * там же — составление карточек слов, чтобы его ход был виден на вкладке карточек, откуда бы его ни запустили.
 */
export interface TopicActions {
  addMaterial: () => void
  addWords: () => void
  words: VocabularyRun
}

/** Действия экрана темы, доступные вкладке. */
export function useTopicActions(): TopicActions {
  return useOutletContext<TopicActions>()
}
