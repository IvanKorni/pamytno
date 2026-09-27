import { useState } from 'react'
import { BookOpen, Search } from 'lucide-react'
import { EditCardModal, FlashcardRow, useCards, useDeleteCard, type Flashcard } from '@/modules/deck'
import { EmptyState, ErrorState, InlineLoading } from '@/shared'
import { useTopicId } from './useTopicId'

/** Экран карточек темы: поиск, просмотр ответа, изменение и удаление. */
export function CardsPage() {
  const topicId = useTopicId()
  const [editing, setEditing] = useState<Flashcard>()
  const cards = useCards(topicId)
  if (cards.isLoading) return <InlineLoading />
  if (cards.isError) return <ErrorState onRetry={() => cards.refetch()} />
  const list = cards.data || []
  return (
    <section className="topic-section">
      <div className="section-heading">
        <div>
          <h2>
            Карточки <span className="count-badge">{list.length}</span>
          </h2>
          <p className="muted">Открывайте ответ, редактируйте и возвращайтесь к сложному.</p>
        </div>
      </div>
      {list.length ? (
        <SearchableCards topicId={topicId} cards={list} onEdit={setEditing} />
      ) : (
        <EmptyState
          icon={<BookOpen size={24} />}
          title="Карточек пока нет"
          text="Одобрите вопросы и создайте из них первую колоду."
        />
      )}
      {editing && <EditCardModal card={editing} close={() => setEditing(undefined)} />}
    </section>
  )
}

/** Свойства списка карточек с поиском. */
interface SearchableCardsProps {
  topicId: string
  cards: Flashcard[]
  onEdit: (card: Flashcard) => void
}

/** Список карточек с поиском по вопросу и ответу. */
function SearchableCards({ topicId, cards, onEdit }: SearchableCardsProps) {
  const [search, setSearch] = useState('')
  const remove = useDeleteCard(topicId)
  const query = search.toLowerCase()
  const filtered = cards.filter((card) => `${card.front} ${card.back}`.toLowerCase().includes(query))
  const confirmRemove = (id: string) => window.confirm('Удалить карточку?') && remove.mutate(id)
  return (
    <>
      <div className="search-box">
        <Search size={17} />
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Поиск по карточкам" />
      </div>
      {filtered.length ? (
        <div className="card-list">
          {filtered.map((card) => (
            <FlashcardRow key={card.id} card={card} edit={() => onEdit(card)} remove={() => confirmRemove(card.id)} />
          ))}
        </div>
      ) : (
        <EmptyState title="Ничего не найдено" text="Попробуйте изменить поисковый запрос." />
      )}
    </>
  )
}
