import { Check, X } from 'lucide-react'
import { EmptyState } from '@/shared'
import type { Question, QuestionDecision } from '../model/types'

/** Свойства просмотра вопросов по одному. */
interface QuestionReviewProps {
  current?: Question
  position: number
  total: number
  onDecide: (decision: QuestionDecision) => void
  deciding: boolean
}

/** Просмотр новых вопросов по одному: «Хочу изучить» или «Не изучать». */
export function QuestionReview({ current, position, total, onDecide, deciding }: QuestionReviewProps) {
  if (!current) {
    return (
      <EmptyState
        icon={<Check size={24} />}
        title="Все вопросы просмотрены"
        text="Создайте карточки из выбранных вопросов или вернитесь к списку."
      />
    )
  }
  return (
    <div className="review-panel">
      <div className="review-count">
        {position} <span>/ {total}</span>
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
        <button className="decision-button reject" disabled={deciding} onClick={() => onDecide('REJECT')}>
          <X size={19} /> Не изучать
        </button>
        <button className="decision-button approve" disabled={deciding} onClick={() => onDecide('APPROVE')}>
          Хочу изучить <Check size={19} />
        </button>
      </div>
    </div>
  )
}
