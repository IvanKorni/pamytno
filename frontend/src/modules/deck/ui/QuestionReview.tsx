import { EmptyState, isShortText, RichInline, RichText, useKeyboardShortcuts } from '@/shared'
import type { Question, QuestionDecision } from '../model/types'

/** Свойства просмотра вопросов по одному. */
interface QuestionReviewProps {
  current?: Question
  position: number
  total: number
  onDecide: (decision: QuestionDecision) => void
  deciding: boolean
}

/** Просмотр новых вопросов по одному: «Хочу изучить» или «Не изучать», в том числе стрелками. */
export function QuestionReview({ current, position, total, onDecide, deciding }: QuestionReviewProps) {
  const decide = (decision: QuestionDecision) => current && !deciding && onDecide(decision)
  useKeyboardShortcuts({ ArrowLeft: () => decide('REJECT'), ArrowRight: () => decide('APPROVE') })
  if (!current) {
    return (
      <EmptyState
        title="Все вопросы просмотрены."
        text="Создайте карточки из выбранных вопросов или переключитесь на список."
      />
    )
  }
  return (
    <div className="review-panel">
      <div className="mono review-count">
        {position} / {total}
      </div>
      <QuestionText text={current.text} />
      {current.sourceFragment && (
        <details className="review-source">
          <summary>Источник</summary>
          <p>{current.sourceFragment}</p>
        </details>
      )}
      <div className="review-actions">
        <button className="button button-secondary" disabled={deciding} onClick={() => onDecide('REJECT')}>
          Не изучать
        </button>
        <button className="button button-primary" disabled={deciding} onClick={() => onDecide('APPROVE')}>
          Хочу изучить
        </button>
      </div>
      <div className="mono key-hint">← не изучать · изучить →</div>
    </div>
  )
}

/** Текст вопроса: короткий — крупно, длинный (задача с вариантами ответа) — обычным текстом с разметкой. */
function QuestionText({ text }: { text: string }) {
  if (isShortText(text)) {
    return (
      <h2 className="review-question">
        <RichInline text={text} />
      </h2>
    )
  }
  return <RichText text={text} className="review-question-text" />
}
