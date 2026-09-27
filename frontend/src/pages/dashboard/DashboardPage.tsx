import { useNavigate } from 'react-router-dom'
import { BookOpen, Plus } from 'lucide-react'
import { useDashboard, type TopicProgress } from '@/modules/learning'
import { useTopics, type Topic } from '@/modules/topic'
import { EmptyState, ErrorState, PageLoading, plural } from '@/shared'
import { TodayCard } from './TodayCard'
import { TopicCard } from './TopicCard'

/** Главный экран: карточки к повторению сегодня и темы пользователя с прогрессом. */
export function DashboardPage() {
  const navigate = useNavigate()
  const dashboard = useDashboard()
  const topics = useTopics()
  if (dashboard.isLoading || topics.isLoading) return <PageLoading />
  if (dashboard.isError || topics.isError) {
    return (
      <ErrorState
        onRetry={() => {
          dashboard.refetch()
          topics.refetch()
        }}
      />
    )
  }
  const progressById = new Map((dashboard.data?.topics || []).map((item) => [item.topicId, item]))
  const list = topics.data || []
  const startLearning = () => {
    const first = list.find((topic) => (progressById.get(topic.id)?.dueToday || 0) > 0)
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
      <TodayCard dueCount={dashboard.data?.dueToday ?? 0} onStart={startLearning} />
      <div className="section-heading">
        <div>
          <h2>Мои темы</h2>
          <p className="muted">
            {list.length} {plural(list.length, 'тема', 'темы', 'тем')}
          </p>
        </div>
      </div>
      <TopicGrid topics={list} progressById={progressById} />
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
