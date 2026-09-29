import { Link } from 'react-router-dom'
import { useTopicProgress, type TopicProgress } from '@/modules/learning'
import type { Topic } from '@/modules/topic'
import { formatPercent, plural } from '@/shared'

/** Свойства шапки темы. */
interface TopicHeaderProps {
  topic: Topic
  onAddMaterial: () => void
  onAddWords: () => void
  onEdit: () => void
}

/** Шапка темы: крупное название, описание, прогресс одной строкой и главные действия. */
export function TopicHeader({ topic, onAddMaterial, onAddWords, onEdit }: TopicHeaderProps) {
  const progress = useTopicProgress(topic.id).data
  return (
    <header className="topic-header">
      <h1>{topic.title}</h1>
      {topic.description && <p className="topic-description">{topic.description}</p>}
      {progress && <TopicStats progress={progress} />}
      <div className="button-row topic-actions">
        {Boolean(progress?.totalCards) && (
          <>
            <Link className="button button-primary" to={`/topics/${topic.id}/learn`}>
              Продолжить обучение
            </Link>
            <Link className="button button-secondary" to={`/topics/${topic.id}/cards`}>
              Все карточки
            </Link>
          </>
        )}
        <button className="button button-secondary" onClick={onAddMaterial}>
          Добавить материал
        </button>
        <button className="button button-secondary" onClick={onAddWords}>
          Добавить слова
        </button>
        <button className="link-button is-muted" onClick={onEdit}>
          Изменить тему
        </button>
      </div>
    </header>
  )
}

/** Прогресс темы одной строкой: доля изученного, число карточек и сколько повторить сегодня. */
function TopicStats({ progress }: { progress: TopicProgress }) {
  const { totalCards, dueToday } = progress
  return (
    <div className="topic-stats">
      <span>
        <strong>{formatPercent(progress.progress)}</strong> изучено
      </span>
      <span>
        {totalCards} {plural(totalCards, 'карточка', 'карточки', 'карточек')}
      </span>
      <span>{dueToday ? `${dueToday} повторить сегодня` : 'на сегодня всё'}</span>
    </div>
  )
}
