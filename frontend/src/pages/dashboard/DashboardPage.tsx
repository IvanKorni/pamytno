import { useNavigate } from 'react-router-dom'
import { BookOpen, Plus } from 'lucide-react'
import { useDashboard, type Dashboard, type TopicProgress } from '@/modules/learning'
import { useTopics, type Topic } from '@/modules/topic'
import { EmptyState, ErrorState, PageLoading, plural } from '@/shared'
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

/** Обзор: сколько карточек ждут повторения сейчас и темы пользователя с прогрессом. */
function DashboardContent({ dashboard, topics }: { dashboard: Dashboard; topics: Topic[] }) {
  const navigate = useNavigate()
  const progressById = new Map(dashboard.topics.map((item) => [item.topicId, item]))
  const startLearning = () => {
    const first = topics.find((topic) => (progressById.get(topic.id)?.dueCards ?? 0) > 0)
    if (first) navigate(`/topics/${first.id}/learn`)
  }
  return (
    <div className="content-wrap">
      <header className="page-header">
        <div>
          <div className="eyebrow">Твой обзор</div>
          <h1>Что изучим сегодня?</h1>
          <p className="muted">Небольшие шаги складываются в устойчивые знания.</p>
        </div>
        <NewTopicButton />
      </header>
      <TodayCard dueNow={dashboard.dueCards} dueToday={dashboard.dueToday} onStart={startLearning} />
      <div className="section-heading">
        <div>
          <h2>Мои темы</h2>
          <p className="muted">
            {topics.length} {plural(topics.length, 'тема', 'темы', 'тем')}
          </p>
        </div>
      </div>
      <TopicGrid topics={topics} progressById={progressById} />
    </div>
  )
}

/** Сетка тем или приглашение создать первую. */
function TopicGrid({ topics, progressById }: { topics: Topic[]; progressById: Map<string, TopicProgress> }) {
  if (!topics.length) {
    return (
      <EmptyState
        icon={<BookOpen size={24} />}
        title="Здесь пока пусто"
        text="Создайте первую тему — добавьте материал, а Памятно поможет выделить главное."
        action={<NewTopicButton label="Создать тему" />}
      />
    )
  }
  return (
    <div className="topic-grid">
      {topics.map((topic) => (
        <TopicCard key={topic.id} topic={topic} progress={progressById.get(topic.id)} />
      ))}
    </div>
  )
}

/** Кнопка перехода к созданию темы. */
function NewTopicButton({ label = 'Новая тема' }: { label?: string }) {
  const navigate = useNavigate()
  return (
    <button className="button button-primary" onClick={() => navigate('/topics/new')}>
      <Plus size={17} /> {label}
    </button>
  )
}
