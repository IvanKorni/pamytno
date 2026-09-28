import { useState } from 'react'
import { Link } from 'react-router-dom'
import { EditCardModal, useCards, useDeleteCard, type Flashcard } from '@/modules/deck'
import { useRefreshProgress } from '@/modules/learning'
import { EmptyState, ErrorState, InlineLoading } from '@/shared'
import { CardsBrowser } from './CardsBrowser'
import { useTopicId } from './useTopicId'

/** Экран карточек темы: просмотр по одной или все сразу, поиск, изменение и удаление. */
export function CardsPage() {
  const topicId = useTopicId()
  const [editing, setEditing] = useState<Flashcard>()
  const cards = useCards(topicId)
  const refreshProgress = useRefreshProgress(topicId)
  const remove = useDeleteCard(topicId, refreshProgress)
  if (cards.isLoading) return <InlineLoading />
  if (cards.isError) return <ErrorState onRetry={() => cards.refetch()} />
  const list = cards.data || []
  const confirmRemove = (card: Flashcard) =>
    window.confirm('Удалить карточку?') && remove.mutate(card.id, { onSuccess: () => setEditing(undefined) })
  return (
    <section>
      {list.length ? (
        <CardsBrowser cards={list} onEdit={setEditing} />
      ) : (
        <EmptyState
          title="Здесь пока нет карточек."
          text="Отберите вопросы — из них получатся карточки."
          action={
            <Link className="button button-secondary" to={`/topics/${topicId}/questions`}>
              К вопросам
            </Link>
          }
        />
      )}
      {editing && (
        <EditCardModal
          card={editing}
          close={() => setEditing(undefined)}
          onDelete={() => confirmRemove(editing)}
          deleting={remove.isPending}
        />
      )}
    </section>
  )
}
