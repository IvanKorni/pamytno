import { Check, ChevronRight, X } from 'lucide-react'
import { InlineError } from '@/shared'
import type { DueCard } from '../model/types'

/** Число этапов повторения: на этапах 0–5 карточка учится, после шестого считается изученной. */
const STAGES = 6

/** Свойства карточки в режиме обучения. */
interface LearningCardProps {
  card: DueCard
  revealed: boolean
  reveal: () => void
  answer: (result: 'REMEMBER' | 'FORGOT') => void
  answering: boolean
  error?: string
}

/** Карточка в режиме обучения: вопрос, ответ по запросу и оценка «Помню» / «Не помню». */
export function LearningCard({ card, revealed, reveal, answer, answering, error }: LearningCardProps) {
  return (
    <div className="learning-card">
      <div className="learning-card-meta">
        <span>
          Этап {Math.min(card.stage + 1, STAGES)} из {STAGES}
        </span>
        <span>{card.totalReviews ? `${card.totalReviews} повторений` : 'Новая карточка'}</span>
      </div>
      <div className="learning-card-body">
        <div className="question-label">ВОПРОС</div>
        <h1>{card.front}</h1>
        {revealed && <Answer text={card.back} />}
      </div>
      {error && <InlineError message={`Ответ не сохранён: ${error}`} />}
      {revealed ? (
        <div className="learning-actions">
          <button className="decision-button reject" disabled={answering} onClick={() => answer('FORGOT')}>
            <X size={19} /> Не помню
          </button>
          <button className="decision-button approve" disabled={answering} onClick={() => answer('REMEMBER')}>
            Помню <Check size={19} />
          </button>
        </div>
      ) : (
        <button className="button button-dark reveal-button" onClick={reveal}>
          Показать ответ <ChevronRight size={17} />
        </button>
      )}
    </div>
  )
}

/** Открытый ответ карточки. */
function Answer({ text }: { text: string }) {
  return (
    <div className="answer">
      <div className="answer-line" />
      <div className="question-label">ОТВЕТ</div>
      <p>{text}</p>
    </div>
  )
}
