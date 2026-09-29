import { useState } from 'react'
import { Link } from 'react-router-dom'
import { EditCardModal, useCards, useDeleteCard, type Flashcard } from '@/modules/deck'
import { useRefreshProgress } from '@/modules/learning'
import { EmptyState, ErrorState, InlineError, InlineLoading, ProcessingCard } from '@/shared'
import { CardsBrowser } from './CardsBrowser'
import { useTopicActions } from './topicActions'
import { useTopicId } from './useTopicId'

/** Экран карточек темы: ход составления карточек слов, просмотр по одной или все сразу, поиск, изменение и удаление. */
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
      <WordsStatus />
      {list.length ? <CardsBrowser cards={list} onEdit={setEditing} /> : <NoCards topicId={topicId} />}
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

/** Ход составления карточек слов или ошибка последней задачи. */
function WordsStatus() {
  const { words } = useTopicActions()
  if (words.running) return <ProcessingCard title="Составляем карточки слов" subtitle="можно уйти с экрана" />
  return words.jobError ? <InlineError message={words.jobError} /> : null
}

/** Карточек пока нет: их дают отобранные вопросы или английские слова. Пока слова составляются — пусто. */
function NoCards({ topicId }: { topicId: string }) {
  const { addWords, words } = useTopicActions()
  if (words.running) return null
  return (
    <EmptyState
      title="Здесь пока нет карточек."
      text="Отберите вопросы или добавьте английские слова — из них получатся карточки."
      action={
        <div className="button-row">
          <button className="button button-primary" onClick={addWords}>
            Добавить слова
          </button>
          <Link className="button button-secondary" to={`/topics/${topicId}/questions`}>
            К вопросам
          </Link>
        </div>
      }
    />
  )
}
