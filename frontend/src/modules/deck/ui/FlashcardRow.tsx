import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { plainText, RichText, splitFirstLine } from '@/shared'
import type { Flashcard } from '../model/types'

/** Свойства строки карточки. */
interface FlashcardRowProps {
  card: Flashcard
  edit: () => void
  remove: () => void
}

/**
 * Карточка в списке: первая строка вопроса, по нажатию — остальной вопрос (например, варианты ответа) и ответ,
 * изменение и удаление. Раскрытый текст лежит вне кнопки: списки и код внутри кнопки недопустимы.
 */
export function FlashcardRow({ card, edit, remove }: FlashcardRowProps) {
  const [open, setOpen] = useState(false)
  const { first, rest } = splitFirstLine(card.front)
  return (
    <article className="flashcard-row">
      <div className="flashcard-main">
        <button className="flashcard-toggle" onClick={() => setOpen(!open)} aria-expanded={open}>
          <span className={`flashcard-front ${open ? '' : 'is-clamped'}`}>{plainText(first)}</span>
          <span className="flashcard-chevron">{open ? '−' : '+'}</span>
        </button>
        {open && (
          <div className="flashcard-details">
            {rest && <RichText text={rest} className="flashcard-question" />}
            <RichText text={card.back} className="flashcard-back" />
          </div>
        )}
      </div>
      <div className="row-actions">
        <button className="text-button" onClick={edit}>
          Изменить
        </button>
        <button className="icon-button subtle" onClick={remove} title="Удалить" aria-label="Удалить карточку">
          <Trash2 size={16} />
        </button>
      </div>
    </article>
  )
}
