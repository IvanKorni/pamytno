import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDashboard, type Dashboard, type TopicProgress } from '@/modules/learning'
import { CreateTopicModal, useTopics, type Topic } from '@/modules/topic'
import { APP_NAME, EmptyState, ErrorState, PageLoading, plural } from '@/shared'
import { TodayCard } from './TodayCard'
import { TopicCard } from './TopicCard'

/** Главный экран: загрузка обзора и тем пользователя. */
export function DashboardPage() {
  const dashboard = useDashboard()
  const topics = useTopics()
  if (dashboard.isLoading || topics.isLoading) return <PageLoading />
  if (!dashboard.data || !topics.data) {
    return (
      <ErrorState
        onRetry={() => {
          dashboard.refetch()
          topics.refetch()
        }}
      />
    )
  }
  return <DashboardContent dashboard={dashboard.data} topics={topics.data} />
}

/** Обзор: темы пользователя с прогрессом и сколько карточек ждут повторения сейчас. */
function DashboardContent({ dashboard, topics }: { dashboard: Dashboard; topics: Topic[] }) {
  const navigate = useNavigate()
  const [creating, setCreating] = useState(false)
  const progressById = new Map(dashboard.topics.map((item) => [item.topicId, item]))
  const startLearning = () => {
    const first = topics.find((topic) => (progressById.get(topic.id)?.dueCards ?? 0) > 0)
    if (first) navigate(`/topics/${first.id}/learn`)
  }
  return (
    <section>
      <div className="section-title">
        <h1>Темы</h1>
        <span className="mono">{summary(topics.length, dashboard.totalCards)}</span>
      </div>
      {topics.length > 0 && (
        <TodayCard dueNow={dashboard.dueCards} dueToday={dashboard.dueToday} onStart={startLearning} />
      )}
      <TopicGrid topics={topics} progressById={progressById} onCreate={() => setCreating(true)} />
      {creating && (
        <CreateTopicModal close={() => setCreating(false)} onCreated={(topicId) => navigate(`/topics/${topicId}`)} />
      )}
    </section>
  )
}

/** Свойства сетки тем. */
interface TopicGridProps {
  topics: Topic[]
  progressById: Map<string, TopicProgress>
  onCreate: () => void
}

/** Сетка тем с плиткой «Новая тема» или приглашение создать первую. */
function TopicGrid({ topics, progressById, onCreate }: TopicGridProps) {
  if (!topics.length) {
    return (
      <EmptyState
        title="Здесь пока пусто"
        text={`Создайте первую тему — добавьте материал, а ${APP_NAME} поможет выделить главное.`}
        action={
          <button className="button button-primary" onClick={onCreate}>
            Создать тему
          </button>
        }
      />
    )
  }
  return (
    <div className="topic-grid">
      {topics.map((topic) => (
        <TopicCard key={topic.id} topic={topic} progress={progressById.get(topic.id)} />
      ))}
      <button className="topic-new" onClick={onCreate}>
        + Новая тема
      </button>
    </div>
  )
}

/** Сводка над сеткой: «4 темы · 422 карточки». */
function summary(topics: number, cards: number): string {
  return `${topics} ${plural(topics, 'тема', 'темы', 'тем')} · ${cards} ${plural(cards, 'карточка', 'карточки', 'карточек')}`
}
