import { Link } from 'react-router-dom'
import type { LearningState } from '@/modules/learning'

/** Свойства верхней строки обучения. */
interface LearningTopBarProps {
  topicId: string
  title: string
  state: LearningState
}

/**
 * Верхняя строка обучения: тема, номер карточки из всех в сессии и выход, под ней — тонкая полоса пройденного.
 * Забытая карточка, которая вернулась в очередь, увеличивает общее число.
 */
export function LearningTopBar({ topicId, title, state }: LearningTopBarProps) {
  const active = state.phase === 'active'
  const answered = active ? state.remembered + state.forgotten : 0
  const total = active ? answered + state.queue.length : 0
  const done = state.phase === 'finished' ? 100 : 0
  return (
    <>
      <div className="learning-top">
        <span className="learning-topic">{title}</span>
        <span className="mono learning-count">{active && `${answered + 1} / ${total}`}</span>
        <span className="learning-exit">
          <Link to={`/topics/${topicId}`}>{active ? 'Закончить' : 'К теме'}</Link>
        </span>
      </div>
      <div className="learning-bar">
        <span style={{ width: `${total ? (answered / total) * 100 : done}%` }} />
      </div>
    </>
  )
}
