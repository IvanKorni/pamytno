import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { CardChanges, Flashcard } from '../model/types'
import { deleteCard, listCards, updateCard } from './deckApi'
import { deckKeys } from './deckKeys'

/** Карточки темы. */
export function useCards(topicId: string) {
  return useQuery({ queryKey: deckKeys.cards(topicId), queryFn: () => listCards(topicId) })
}

/** Удаление карточки. */
export function useDeleteCard(topicId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteCard,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: deckKeys.cards(topicId) }),
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
