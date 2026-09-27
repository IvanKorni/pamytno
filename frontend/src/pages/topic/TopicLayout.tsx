import { useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { BookOpen, ChevronRight, CircleHelp, FileText, GraduationCap, Play } from 'lucide-react'
import { learningKeys, useTopicProgress } from '@/modules/learning'
import { EditTopicModal, useTopic, type Topic } from '@/modules/topic'
import { ErrorState, formatPercent, PageLoading, Stat } from '@/shared'
import { useTopicId } from './useTopicId'

/** Каркас экранов темы: шапка, статистика, вкладки и вложенный экран. */
export function TopicLayout() {
  const topicId = useTopicId()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const topic = useTopic(topicId)
  const [showEdit, setShowEdit] = useState(false)
  const onDeleted = () => {
    queryClient.invalidateQueries({ queryKey: learningKeys.dashboard })
    navigate('/')
  }
  if (topic.isLoading) return <PageLoading />
  if (topic.isError || !topic.data) return <ErrorState />
  return (
    <div className="content-wrap topic-wrap">
      <div className="topic-breadcrumb">
        <Link to="/">Обзор</Link>
        <ChevronRight size={14} /> <span>{topic.data.title}</span>
      </div>
      <TopicHeader topic={topic.data} onEdit={() => setShowEdit(true)} />
      <TopicStats topicId={topicId} />
      <nav className="topic-tabs">
        <TopicTab to="materials" label="Материалы" icon={<FileText size={16} />} />
        <TopicTab to="questions" label="Вопросы" icon={<CircleHelp size={16} />} />
        <TopicTab to="cards" label="Карточки" icon={<BookOpen size={16} />} />
        <TopicTab to="progress" label="Прогресс" icon={<GraduationCap size={16} />} />
      </nav>
      <Outlet />
      {showEdit && <EditTopicModal topic={topic.data} close={() => setShowEdit(false)} onDeleted={onDeleted} />}
    </div>
  )
}

/** Шапка темы: название, описание, «Учить» и настройки. */
function TopicHeader({ topic, onEdit }: { topic: Topic; onEdit: () => void }) {
  return (
    <header className="topic-header">
      <div>
        <div className="eyebrow">Тема</div>
        <h1>{topic.title}</h1>
        {topic.description && <p className="muted">{topic.description}</p>}
      </div>
      <div className="topic-header-actions">
        <Link className="button button-primary" to={`/topics/${topic.id}/learn`}>
          <Play size={16} fill="currentColor" /> Учить
        </Link>
        <button className="icon-button" title="Изменить тему" onClick={onEdit}>
          …
        </button>
      </div>
    </header>
  )
}

/** Строка статистики темы: прогресс, число карточек и сколько повторить сегодня. */
function TopicStats({ topicId }: { topicId: string }) {
  const progress = useTopicProgress(topicId)
  return (
    <div className="topic-stats">
      <Stat label="Прогресс" value={formatPercent(progress.data?.progress || 0)} />
      <Stat label="Карточек" value={progress.data?.totalCards || 0} />
      <Stat label="Повторить сегодня" value={progress.data?.dueToday || 0} accent />
    </div>
  )
}

/** Вкладка экрана темы. */
function TopicTab({ to, label, icon }: { to: string; label: string; icon: ReactNode }) {
  return (
    <NavLink to={to}>
      {icon}
      {label}
    </NavLink>
  )
}
