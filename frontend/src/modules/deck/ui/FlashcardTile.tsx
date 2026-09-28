import { useState } from 'react'
import { plainText, RichText, splitFirstLine } from '@/shared'
import type { Flashcard } from '../model/types'

/** Свойства карточки для просмотра. */
interface FlashcardTileProps {
  card: Flashcard
  large?: boolean
  counter?: string
}

/**
 * Карточка для просмотра: первая строка вопроса, по нажатию — остальной вопрос (например, варианты ответа)
 * и ответ. Раскрытый текст лежит вне кнопки: списки и код внутри кнопки недопустимы.
 */
export function FlashcardTile({ card, large = false, counter }: FlashcardTileProps) {
  const [open, setOpen] = useState(false)
  const { first, rest } = splitFirstLine(card.front)
  return (
    <article className={tileClass(large, open)}>
      {counter && <span className="mono">{counter}</span>}
      <button className="flashcard-toggle" onClick={() => setOpen(!open)} aria-expanded={open}>
        <span className={open ? 'flashcard-front' : 'flashcard-front is-clamped'}>{plainText(first)}</span>
        {!open && <span className="flashcard-hint">Показать ответ</span>}
      </button>
      {open && <FlashcardDetails rest={rest} back={card.back} />}
    </article>
  )
}

/** Раскрытая карточка: остаток вопроса (например, варианты ответа) и ответ с разметкой. */
function FlashcardDetails({ rest, back }: { rest: string; back: string }) {
  return (
    <div className="flashcard-details">
      {rest && <RichText text={rest} className="flashcard-question" />}
      <RichText text={back} className="flashcard-back" />
    </div>
  )
}

/** Классы карточки: крупная ли она и открыт ли ответ. */
function tileClass(large: boolean, open: boolean): string {
  return ['flashcard', large && 'is-large', open && 'is-open'].filter(Boolean).join(' ')
}
