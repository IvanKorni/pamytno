import { Link, useNavigate } from 'react-router-dom'
import { useQuestionGeneration, useQuestions } from '@/modules/deck'
import { InlineError } from '@/shared'

/** Свойства действий под материалами. */
interface MaterialActionsProps {
  topicId: string
  showContent: boolean
  toggleContent: () => void
}

/** Действия под готовыми материалами: создать вопросы или перейти к ним и показать исходный текст. */
export function MaterialActions({ topicId, showContent, toggleContent }: MaterialActionsProps) {
  return (
    <div className="materials-actions">
      <QuestionsAction topicId={topicId} />
      <button className="button button-secondary" onClick={toggleContent} aria-expanded={showContent}>
        {showContent ? 'Скрыть исходный текст' : 'Исходный текст'}
      </button>
    </div>
  )
}

/**
 * Главное действие: пока вопросов нет — «Создать вопросы» с переходом на вкладку вопросов, где видна генерация;
 * когда вопросы уже есть — ссылка на них: повторная генерация добавила бы дубликаты.
 */
function QuestionsAction({ topicId }: { topicId: string }) {
  const navigate = useNavigate()
  const questions = useQuestions(topicId)
  const generation = useQuestionGeneration(topicId)
  const toQuestions = () => navigate(`/topics/${topicId}/questions`)
  if (generation.running) {
    return (
      <button className="button button-primary" onClick={toQuestions}>
        Вопросы создаются…
      </button>
    )
  }
  if (questions.data?.length !== 0) {
    return (
      <Link className="button button-primary" to={`/topics/${topicId}/questions`}>
        К вопросам
      </Link>
    )
  }
  return (
    <>
      {generation.error && <InlineError message={generation.error} />}
      <button className="button button-primary" onClick={() => generation.launchThen(toQuestions)}>
        Создать вопросы
      </button>
    </>
  )
}
