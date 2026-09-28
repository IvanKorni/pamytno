import { Link } from 'react-router-dom'
import type { TopicProgress } from '@/modules/learning'
import { topicStatusLabel, type Topic } from '@/modules/topic'
import { formatPercent, plural } from '@/shared'

/** Показатели темы, которые нужны плитке. */
type CardStats = Pick<TopicProgress, 'progress' | 'totalCards' | 'dueToday'>

/** Показатели темы, по которой ещё нет карточек. */
const NO_PROGRESS: CardStats = { progress: 0, totalCards: 0, dueToday: 0 }

/**
 * Плитка темы на обзоре: название, доля изученного, число карточек и статус материалов.
 * Вся плитка кликабельна через растянутую ссылку в заголовке — так она доступна и с клавиатуры.
 */
export function TopicCard({ topic, progress = NO_PROGRESS }: { topic: Topic; progress?: CardStats }) {
  const percent = Math.min(progress.progress, 100)
  return (
    <article className="topic-tile">
      <div className="topic-tile-face">
        <h2 className="topic-tile-title">
          <Link className="topic-tile-link" to={`/topics/${topic.id}`}>
            {topic.title}
          </Link>
        </h2>
        <div className="topic-tile-progress">
          <span className="mono">{progress.totalCards ? `${formatPercent(percent)} изучено` : 'нет карточек'}</span>
          <span className="progress-line">
            <span style={{ width: `${percent}%` }} />
          </span>
        </div>
      </div>
      <div className="topic-tile-meta">
        <span>{cardsLabel(progress)}</span>
        <span className="mono">{topicStatusLabel(topic.status).toLowerCase()}</span>
      </div>
    </article>
  )
}

/** Подпись под плиткой: число карточек и сколько повторить сегодня. */
function cardsLabel({ totalCards, dueToday }: CardStats): string {
  if (!totalCards) return 'Добавьте первый материал'
  const cards = `${totalCards} ${plural(totalCards, 'карточка', 'карточки', 'карточек')}`
  return dueToday ? `${cards} · ${dueToday} повторить сегодня` : `${cards} · всё повторено`
}
