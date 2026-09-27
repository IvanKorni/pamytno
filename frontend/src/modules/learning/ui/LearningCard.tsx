import { Check, ChevronRight, X } from 'lucide-react'
import type { DueCard } from '../model/types'

/** Свойства карточки в режиме обучения. */
interface LearningCardProps {
  card: DueCard
  revealed: boolean
  reveal: () => void
  answer: (result: 'REMEMBER' | 'FORGOT') => void
  answering: boolean
}

/** Карточка в режиме обучения: вопрос, ответ по запросу и оценка «Помню» / «Не помню». */
export function LearningCard({ card, revealed, reveal, answer, answering }: LearningCardProps) {
  return (
    <div className="learning-card">
      <div className="learning-card-meta">
        <span>Этап {card.stage} из 6</span>
        <span>{card.totalReviews ? `${card.totalReviews} повторений` : 'Новая карточка'}</span>
      </div>
      <div className="learning-card-body">
        <div className="question-label">ВОПРОС</div>
        <h1>{card.front}</h1>
        {revealed && (
          <div className="answer">
            <div className="answer-line" />
            <div className="question-label">ОТВЕТ</div>
            <p>{card.back}</p>
          </div>
        )}
      </div>
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
