import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useToast } from '@/shared'
import type { CardChanges, Flashcard } from '../model/types'
import { deleteCard, listCards, updateCard } from './deckApi'
import { deckKeys } from './deckKeys'

/** Карточки темы. */
export function useCards(topicId: string) {
  return useQuery({ queryKey: deckKeys.cards(topicId), queryFn: () => listCards(topicId) })
}

/**
 * Удаление карточки; ошибка показывается уведомлением.
 *
 * @param onDeleted что перечитать в других модулях, например прогресс темы
 */
export function useDeleteCard(topicId: string, onDeleted: () => void) {
  const queryClient = useQueryClient()
  const showToast = useToast((toast) => toast.show)
  return useMutation({
    mutationFn: deleteCard,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: deckKeys.cards(topicId) })
      onDeleted()
    },
    onError: (error) => showToast(`Не удалось удалить карточку: ${error.message}`, 'error'),
  })
}

/** Сохранение вопроса и ответа карточки. */
export function useUpdateCard(card: Flashcard, onSaved: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (changes: CardChanges) => updateCard(card.id, changes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: deckKeys.cards(card.topicId) })
      onSaved()
    },
  })
}
