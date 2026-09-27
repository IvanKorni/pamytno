import { Link, useNavigate } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import type { TopicProgress } from '@/modules/learning'
import { topicStatusLabel, type Topic } from '@/modules/topic'
import { formatPercent, plural } from '@/shared'

/** Показатели темы, которые нужны карточке. */
type CardStats = Pick<TopicProgress, 'progress' | 'totalCards' | 'dueCards' | 'dueToday'>

/** Показатели темы, по которой ещё нет карточек. */
const NO_PROGRESS: CardStats = { progress: 0, totalCards: 0, dueCards: 0, dueToday: 0 }

/**
 * Карточка темы на dashboard: статус, прогресс и переход к повторению. Вся карточка кликабельна
 * через растянутую ссылку в заголовке — так она доступна и с клавиатуры.
 */
export function TopicCard({ topic, progress = NO_PROGRESS }: { topic: Topic; progress?: CardStats }) {
  const navigate = useNavigate()
  const percent = progress.progress
  return (
    <article className="topic-card">
      <div className="topic-card-top">
        <span className={`status-dot status-${topic.status.toLowerCase()}`} />{' '}
        <span className="muted small">{topicStatusLabel(topic.status)}</span>
        <ChevronRight size={18} className="topic-arrow" />
      </div>
      <h3>
        <Link className="topic-card-link" to={`/topics/${topic.id}`}>
          {topic.title}
        </Link>
      </h3>
      {topic.description && <p className="muted clamp">{topic.description}</p>}
      <div className="progress-row">
        <strong>{formatPercent(percent)}</strong>
        <span className="muted">{cardsLabel(progress.totalCards)}</span>
      </div>
      <div className="progress-track">
        <span style={{ width: `${Math.min(percent, 100)}%` }} />
      </div>
      <div className="topic-card-foot">
        <span>{progress.dueToday} к повторению сегодня</span>
        {progress.dueCards > 0 && (
          <button className="text-button topic-card-action" onClick={() => navigate(`/topics/${topic.id}/learn`)}>
            Повторить <ChevronRight size={15} />
          </button>
        )}
      </div>
    </article>
  )
}

/** Подпись «N карточек» с правильным склонением. */
function cardsLabel(count: number): string {
  return `${count} ${plural(count, 'карточка', 'карточки', 'карточек')}`
}
