import { useQueryClient } from '@tanstack/react-query'
import { plural, useToast } from '@/shared'
import { generateVocabulary } from '../api/deckApi'
import { deckKeys } from '../api/deckKeys'
import type { VocabularyRequest } from './types'
import { useGenerationRun } from './useGenerationRun'

/**
 * Составление карточек английских слов темы: запуск по тексту и инструкции и опрос задачи. Когда карточки готовы,
 * список карточек перечитывается, итог показывается уведомлением — задача могла закончиться на другом экране.
 *
 * @param onCardsCreated что перечитать в других модулях, например прогресс темы
 */
export function useVocabularyGeneration(topicId: string, onCardsCreated: () => void) {
  const queryClient = useQueryClient()
  const showToast = useToast((toast) => toast.show)
  return useGenerationRun<VocabularyRequest>({
    topicId,
    kind: 'VOCABULARY',
    start: (vocabulary) => generateVocabulary(topicId, vocabulary),
    onReady: (job) => {
      queryClient.invalidateQueries({ queryKey: deckKeys.cards(topicId) })
      onCardsCreated()
      showToast(readyMessage(job.itemsCreated))
    },
    failureText: 'Не удалось составить карточки слов.',
  })
}

/** Состояние составления карточек слов для окна и экрана карточек. */
export type VocabularyRun = ReturnType<typeof useVocabularyGeneration>

/** Итог задачи: сколько карточек слов добавлено или что подходящих слов не нашлось. */
function readyMessage(count: number): string {
  if (!count) return 'Подходящих слов не нашлось — карточки не добавлены'
  return `Готово: ${count} ${plural(count, 'карточка', 'карточки', 'карточек')} слов`
}
