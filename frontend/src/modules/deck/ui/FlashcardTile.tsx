import { useState } from 'react'
import { isShortText, plainText, RichText, splitFirstLine } from '@/shared'
import type { Flashcard } from '../model/types'

/** Свойства карточки для просмотра. */
interface FlashcardTileProps {
  card: Flashcard
  large?: boolean
  counter?: string
}

/**
 * Карточка для просмотра: первая строка вопроса и короткая вторая (у карточки слова — предложение с пропуском),
 * по нажатию — остальной вопрос (например, варианты ответа) и ответ. Раскрытый текст лежит вне кнопки:
 * списки и код внутри кнопки недопустимы.
 */
export function FlashcardTile({ card, large = false, counter }: FlashcardTileProps) {
  const [open, setOpen] = useState(false)
  const { title, subtitle, hidden } = splitFront(card.front)
  return (
    <article className={tileClass(large, open)}>
      {counter && <span className="mono">{counter}</span>}
      <button className="flashcard-toggle" onClick={() => setOpen(!open)} aria-expanded={open}>
        <span className={open ? 'flashcard-front' : 'flashcard-front is-clamped'}>{plainText(title)}</span>
        {subtitle && <span className="flashcard-subtitle">{plainText(subtitle)}</span>}
        {!open && <span className="flashcard-hint">Показать ответ</span>}
      </button>
      {open && <FlashcardDetails rest={hidden} back={card.back} />}
    </article>
  )
}

/**
 * Делит вопрос для свёрнутой карточки: первая строка — заголовок; остаток из одной короткой строки виден сразу,
 * длинный остаток (условие, варианты ответа) открывается вместе с ответом.
 */
function splitFront(front: string): { title: string; subtitle: string; hidden: string } {
  const { first, rest } = splitFirstLine(front)
  if (rest && isShortText(rest)) return { title: first, subtitle: rest, hidden: '' }
  return { title: first, subtitle: '', hidden: rest }
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
