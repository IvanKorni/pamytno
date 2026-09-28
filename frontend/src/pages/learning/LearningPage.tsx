import { Link } from 'react-router-dom'
import { LearningCard, LearningComplete, useLearningSession, type SessionProgress } from '@/modules/learning'
import { useTopic } from '@/modules/topic'
import { EmptyState, ErrorState, PageLoading } from '@/shared'
import { useTopicId } from '../topic/useTopicId'
import { LearningTopBar } from './LearningTopBar'

/**
 * Режим обучения по теме — отдельный экран на всю высоту без шапки, чтобы карточка и кнопки ответа
 * помещались на экран телефона. Смена темы в адресе начинает новую сессию.
 */
export function LearningPage() {
  const topicId = useTopicId()
  return <LearningScreen key={topicId} topicId={topicId} />
}

/** Экран учебной сессии по одной теме. */
function LearningScreen({ topicId }: { topicId: string }) {
  const learning = useLearningSession(topicId)
  const title = useTopic(topicId).data?.title
  return (
    <div className="learning-page">
      <LearningTopBar topicId={topicId} title={title ?? 'Обучение'} state={learning.state} />
      <LearningBody topicId={topicId} title={title} learning={learning} />
    </div>
  )
}

/** Состояние, ответы и действия учебной сессии. */
type Learning = ReturnType<typeof useLearningSession>

/** Свойства основного блока обучения. */
interface LearningBodyProps {
  topicId: string
  title?: string
  learning: Learning
}

/** Основной блок экрана по состоянию сессии. */
function LearningBody({ topicId, title, learning }: LearningBodyProps) {
  const { state } = learning
  switch (state.phase) {
    case 'loading':
      return <PageLoading text="Готовим карточки…" />
    case 'failed':
      return <ErrorState title="Не удалось начать обучение" message={state.message} onRetry={learning.retry} />
    case 'empty':
      return <NothingToReview topicId={topicId} />
    case 'finished':
      return <SessionResults topicId={topicId} title={title} state={state} />
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

/** Пустое состояние: сейчас нет карточек, срок повторения которых наступил. */
function NothingToReview({ topicId }: { topicId: string }) {
  return (
    <div className="learning-message">
      <EmptyState
        title="Пока нечего повторять"
        text="Когда подойдёт срок повторения карточек, они будут ждать вас здесь."
        action={
          <Link className="button button-primary" to={`/topics/${topicId}`}>
            Вернуться к теме
          </Link>
        }
      />
    </div>
  )
}

/** Итоги сессии и переходы дальше. */
function SessionResults({ topicId, title, state }: { topicId: string; title?: string; state: SessionProgress }) {
  return (
    <LearningComplete title={title} remembered={state.remembered} forgotten={state.forgotten}>
      <Link className="button button-primary" to="/">
        На главную
      </Link>
      <Link className="button button-secondary" to={`/topics/${topicId}`}>
        Вернуться к теме
      </Link>
    </LearningComplete>
  )
}
