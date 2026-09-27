import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import type { Flashcard } from '../model/types'

/** Свойства строки карточки. */
interface FlashcardRowProps {
  card: Flashcard
  edit: () => void
  remove: () => void
}

/** Карточка в списке: вопрос, раскрываемый ответ, изменение и удаление. */
export function FlashcardRow({ card, edit, remove }: FlashcardRowProps) {
  const [open, setOpen] = useState(false)
  return (
    <article className="flashcard-row">
      <button className="flashcard-main" onClick={() => setOpen(!open)}>
        <span className="flashcard-front">{card.front}</span>
        <span className="flashcard-chevron">{open ? '−' : '+'}</span>
        {open && <span className="flashcard-back">{card.back}</span>}
      </button>
      <div className="row-actions">
        <button className="text-button" onClick={edit}>
          Изменить
        </button>
        <button className="icon-button subtle" onClick={remove} title="Удалить">
          <Trash2 size={16} />
        </button>
      </div>
    </article>
  )
}
