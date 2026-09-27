import { Check } from 'lucide-react'
import { questionStatusLabel } from '../model/labels'
import type { Question } from '../model/types'

/** Свойства списка вопросов. */
interface QuestionListProps {
  questions: Question[]
  selected: string[]
  setSelected: (ids: string[]) => void
}

/** Список вопросов с множественным выбором новых вопросов. */
export function QuestionList({ questions, selected, setSelected }: QuestionListProps) {
  const selectable = questions.filter((question) => question.status === 'GENERATED')
  const allSelected = selectable.length > 0 && selectable.every((question) => selected.includes(question.id))
  const toggle = (id: string) =>
    setSelected(selected.includes(id) ? selected.filter((item) => item !== id) : [...selected, id])
  return (
    <div className="question-list">
      <div className="list-toolbar">
        <button
          className="text-button"
          onClick={() => setSelected(allSelected ? [] : selectable.map((item) => item.id))}
        >
          {allSelected ? 'Снять всё' : 'Выбрать всё'}
        </button>
        <span className="muted">{selected.length} выбрано</span>
      </div>
      {questions
        .filter((question) => question.status !== 'CARD_CREATED')
        .map((question) => (
          <QuestionItem
            key={question.id}
            question={question}
            checked={selected.includes(question.id)}
            toggle={() => toggle(question.id)}
          />
        ))}
    </div>
  )
}

/** Вопрос в списке с флажком выбора и статусом. */
function QuestionItem({ question, checked, toggle }: { question: Question; checked: boolean; toggle: () => void }) {
  const selectable = question.status === 'GENERATED'
  return (
    <label className={`question-item ${selectable ? '' : 'is-disabled'}`}>
      <input type="checkbox" disabled={!selectable} checked={checked} onChange={toggle} />
      <span className="checkmark">
        <Check size={13} />
      </span>
      <span className="question-item-text">{question.text}</span>
      <span className={`question-status status-${question.status.toLowerCase()}`}>
        {questionStatusLabel(question.status)}
      </span>
    </label>
  )
}
