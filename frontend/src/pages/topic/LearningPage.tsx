import { Link } from 'react-router-dom'
import { ChevronLeft, GraduationCap } from 'lucide-react'
import { LearningCard, LearningComplete, useLearningFlow } from '@/modules/learning'
import { EmptyState } from '@/shared'
import { useTopicId } from './useTopicId'

/** Режим обучения: карточки к повторению по одной с оценкой «Помню» / «Не помню». */
export function LearningPage() {
  const flow = useLearningFlow(useTopicId())
  if (flow.loading) {
    return (
      <div className="learning-loading">
        <div className="spinner" />
        <p>Готовим карточки…</p>
      </div>
    )
  }
  if (flow.finished) {
    return (
      <LearningComplete
        session={flow.finalSession}
        remembered={flow.remembered}
        forgotten={flow.forgotten}
        reset={flow.reset}
      />
    )
  }
  if (!flow.card) {
    return (
      <EmptyState
        icon={<GraduationCap size={24} />}
        title="Пока нечего повторять"
        text="Когда появятся карточки к повторению, они будут ждать вас здесь."
        action={
          <Link className="button button-primary" to="..">
            Вернуться к теме
          </Link>
        }
      />
    )
  }
  const total = flow.session?.cardsTotal || flow.queueLength
  return (
    <div className="learning-page">
      <div className="learning-top">
        <Link to=".." className="back-link">
          <ChevronLeft size={17} /> К теме
        </Link>
        <span className="learning-progress">
          {(flow.session?.cardsTotal || 0) - flow.queueLength + 1} / {total}
        </span>
        <span className="learning-topic">Обучение</span>
      </div>
      <LearningCard
        card={flow.card}
        revealed={flow.revealed}
        reveal={flow.reveal}
        answer={flow.answer}
        answering={flow.answering}
      />
    </div>
  )
}
