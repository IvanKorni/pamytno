import { useState } from 'react'
import { useKeyboardShortcuts } from '@/shared'
import type { Flashcard } from '../model/types'
import { FlashcardTile } from './FlashcardTile'

/** Свойства просмотра карточек по одной. */
interface CardViewerProps {
  cards: Flashcard[]
  onEdit: (card: Flashcard) => void
}

/** Просмотр карточек по одной: крупная карточка, «Назад» и «Далее» по кругу, в том числе стрелками. */
export function CardViewer({ cards, onEdit }: CardViewerProps) {
  const [index, setIndex] = useState(0)
  const position = Math.min(index, cards.length - 1)
  const card = cards[position]
  const step = (delta: number) => setIndex((position + delta + cards.length) % cards.length)
  useKeyboardShortcuts({ ArrowLeft: () => step(-1), ArrowRight: () => step(1) })
  return (
    <div className="card-viewer">
      <FlashcardTile key={card.id} card={card} large counter={`${position + 1} / ${cards.length}`} />
      <div className="card-viewer-nav">
        <button className="button button-secondary button-small" onClick={() => step(-1)}>
          ← Назад
        </button>
        <button className="button button-primary button-small" onClick={() => step(1)}>
          Далее →
        </button>
        <button className="link-button card-viewer-edit" onClick={() => onEdit(card)}>
          Изменить
        </button>
      </div>
    </div>
  )
}

/** Все карточки сеткой: каждую можно раскрыть и изменить. */
export function CardGrid({ cards, onEdit }: CardViewerProps) {
  return (
    <div className="card-grid">
      {cards.map((card) => (
        <div key={card.id} className="card-grid-item">
          <FlashcardTile card={card} />
          <button className="link-button" onClick={() => onEdit(card)}>
            Изменить
          </button>
        </div>
      ))}
    </div>
  )
}
