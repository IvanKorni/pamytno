import { Check, X } from 'lucide-react'
import { EmptyState } from '@/shared'
import type { Question, QuestionDecision } from '../model/types'

/** Свойства просмотра вопросов по одному. */
interface QuestionReviewProps {
  pending: Question[]
  current?: Question
  index: number
  onDecide: (decision: QuestionDecision) => void
}

/** Просмотр новых вопросов по одному: «Хочу изучить» или «Не изучать». */
export function QuestionReview({ pending, current, index, onDecide }: QuestionReviewProps) {
  if (!current) {
    return (
      <EmptyState
        icon={<Check size={24} />}
        title="Все вопросы просмотрены"
        text="Выберите вопросы в списке или создайте карточки из одобренных."
      />
    )
  }
  return (
    <div className="review-panel">
      <div className="review-count">
        {Math.min(index + 1, pending.length)} <span>/ {pending.length}</span>
      </div>
      <div className="review-question">
        <div className="question-label">ВОПРОС</div>
        <h3>{current.text}</h3>
        {current.sourceFragment && (
          <details>
            <summary>Источник</summary>
            <p>{current.sourceFragment}</p>
          </details>
        )}
      </div>
      <div className="review-actions">
        <button className="decision-button reject" onClick={() => onDecide('REJECT')}>
          <X size={19} /> Не изучать
        </button>
        <button className="decision-button approve" onClick={() => onDecide('APPROVE')}>
          Хочу изучить <Check size={19} />
        </button>
      </div>
    </div>
  )
}
