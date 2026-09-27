import { useNavigate } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import type { TopicProgress } from '@/modules/learning'
import { topicStatusLabel, type Topic } from '@/modules/topic'
import { formatPercent, plural } from '@/shared'

/** Карточка темы на dashboard: статус, прогресс и переход к повторению. */
export function TopicCard({ topic, progress }: { topic: Topic; progress?: TopicProgress }) {
  const navigate = useNavigate()
  const percent = progress?.progress || 0
  return (
    <article className="topic-card" onClick={() => navigate(`/topics/${topic.id}`)}>
      <div className="topic-card-top">
        <span className={`status-dot status-${topic.status.toLowerCase()}`} />{' '}
        <span className="muted small">{topicStatusLabel(topic.status)}</span>
        <ChevronRight size={18} className="topic-arrow" />
      </div>
      <h3>{topic.title}</h3>
      {topic.description && <p className="muted clamp">{topic.description}</p>}
      <div className="progress-row">
        <strong>{formatPercent(percent)}</strong>
        <span className="muted">{cardsLabel(progress?.totalCards ?? 0)}</span>
      </div>
      <div className="progress-track">
        <span style={{ width: `${Math.min(percent, 100)}%` }} />
      </div>
      <div className="topic-card-foot">
        <span>{progress?.dueToday ?? 0} к повторению сегодня</span>
        {(progress?.dueCards ?? 0) > 0 && (
          <button
            className="text-button"
            onClick={(e) => {
              e.stopPropagation()
              navigate(`/topics/${topic.id}/learn`)
            }}
          >
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
