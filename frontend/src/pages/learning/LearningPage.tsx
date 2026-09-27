import { Link } from 'react-router-dom'
import { ChevronLeft, GraduationCap } from 'lucide-react'
import { LearningCard, LearningComplete, useLearningSession, type SessionProgress } from '@/modules/learning'
import { useTopic } from '@/modules/topic'
import { EmptyState, ErrorState, plural } from '@/shared'
import { useTopicId } from '../topic/useTopicId'

/**
 * Режим обучения по теме — отдельный экран без шапки темы, чтобы карточка и кнопки ответа
 * помещались на экран телефона. Смена темы в адресе начинает новую сессию.
 */
export function LearningPage() {
  const topicId = useTopicId()
  return <LearningScreen key={topicId} topicId={topicId} />
}

/** Экран учебной сессии по одной теме. */
function LearningScreen({ topicId }: { topicId: string }) {
  const learning = useLearningSession(topicId)
  const topic = useTopic(topicId)
  const { state } = learning
  return (
    <div className="learning-page">
      <div className="learning-top">
        <Link to={`/topics/${topicId}`} className="back-link">
          <ChevronLeft size={17} /> К теме
        </Link>
        <span className="learning-progress">{state.phase === 'active' && remainingLabel(state.queue.length)}</span>
        <span className="learning-topic">{topic.data?.title ?? 'Обучение'}</span>
      </div>
      <LearningBody topicId={topicId} learning={learning} />
    </div>
  )
}

/** Состояние, ответы и действия учебной сессии. */
type Learning = ReturnType<typeof useLearningSession>

/** Основной блок экрана по состоянию сессии. */
function LearningBody({ topicId, learning }: { topicId: string; learning: Learning }) {
  const { state } = learning
  switch (state.phase) {
    case 'loading':
      return <PreparingCards />
    case 'failed':
      return <ErrorState title="Не удалось начать обучение" message={state.message} onRetry={learning.retry} />
    case 'empty':
      return <NothingToReview topicId={topicId} />
    case 'finished':
      return <SessionResults topicId={topicId} state={state} />
    case 'active':
      return (
        <LearningCard
          card={state.queue[0]}
          revealed={state.revealed}
          reveal={learning.reveal}
          answer={learning.answer}
          answering={learning.answering}
          error={learning.answerError}
        />
      )
  }
}

/** Загрузка карточек к повторению. */
function PreparingCards() {
  return (
    <div className="learning-loading">
      <div className="spinner" />
      <p>Готовим карточки…</p>
    </div>
  )
}

/** Пустое состояние: сейчас нет карточек, срок повторения которых наступил. */
function NothingToReview({ topicId }: { topicId: string }) {
  return (
    <EmptyState
      icon={<GraduationCap size={24} />}
      title="Пока нечего повторять"
      text="Когда подойдёт срок повторения карточек, они будут ждать вас здесь."
      action={
        <Link className="button button-primary" to={`/topics/${topicId}`}>
          Вернуться к теме
        </Link>
      }
    />
  )
}

/** Итоги сессии и переходы дальше. */
function SessionResults({ topicId, state }: { topicId: string; state: SessionProgress }) {
  return (
    <LearningComplete remembered={state.remembered} forgotten={state.forgotten}>
      <Link className="button button-primary" to={`/topics/${topicId}`}>
        Вернуться к теме
      </Link>
      <Link className="button button-secondary" to="/">
        На обзор
      </Link>
    </LearningComplete>
  )
}

/** Подпись «Осталось N карточек». */
function remainingLabel(count: number): string {
  return `${plural(count, 'Осталась', 'Осталось', 'Осталось')} ${count} ${plural(count, 'карточка', 'карточки', 'карточек')}`
}
