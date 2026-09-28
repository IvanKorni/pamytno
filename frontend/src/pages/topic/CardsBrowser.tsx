import { useState } from 'react'
import { CardGrid, CardViewer, type Flashcard } from '@/modules/deck'
import { plural, Segmented } from '@/shared'

/** Режим просмотра карточек: по одной или все сразу. */
type Mode = 'one' | 'all'

/** Варианты переключателя режима просмотра. */
const MODES = [
  { value: 'one' as const, label: 'По одной' },
  { value: 'all' as const, label: 'Все' },
]

/** Свойства просмотра карточек. */
interface CardsBrowserProps {
  cards: Flashcard[]
  onEdit: (card: Flashcard) => void
}

/** Карточки с поиском по вопросу и ответу: по одной, как в обучении, или сеткой. */
export function CardsBrowser({ cards, onEdit }: CardsBrowserProps) {
  const [mode, setMode] = useState<Mode>('one')
  const [search, setSearch] = useState('')
  const query = search.trim().toLowerCase()
  const shown = cards.filter((card) => `${card.front} ${card.back}`.toLowerCase().includes(query))
  return (
    <>
      <div className="cards-toolbar">
        <Segmented label="Режим просмотра" options={MODES} value={mode} onChange={setMode} />
        <input
          className="cards-search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Поиск по карточкам"
          aria-label="Поиск по карточкам"
        />
        <span className="mono cards-count">
          {shown.length} {plural(shown.length, 'карточка', 'карточки', 'карточек')}
        </span>
      </div>
      {!shown.length && <p className="muted">Ничего не найдено — попробуйте изменить запрос.</p>}
      {Boolean(shown.length) && mode === 'one' && <CardViewer key={query} cards={shown} onEdit={onEdit} />}
      {Boolean(shown.length) && mode === 'all' && <CardGrid cards={shown} onEdit={onEdit} />}
    </>
  )
}
