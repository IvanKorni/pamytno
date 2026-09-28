import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { learningKeys } from '@/modules/learning'
import { AddMaterialModal, EditTopicModal, useTopic } from '@/modules/topic'
import { ErrorState, HttpError, PageLoading } from '@/shared'
import type { TopicActions } from './topicActions'
import { TopicHeader } from './TopicHeader'
import { useTopicId } from './useTopicId'

/** Какое окно темы открыто. */
type Dialog = 'edit' | 'material'

/** Каркас экранов темы: путь, шапка с действиями, вкладки и вложенный экран. */
export function TopicLayout() {
  const topicId = useTopicId()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const topic = useTopic(topicId)
  const [dialog, setDialog] = useState<Dialog>()
  const close = () => setDialog(undefined)
  const onDeleted = () => {
    queryClient.invalidateQueries({ queryKey: learningKeys.dashboard })
    navigate('/')
  }
  if (topic.isLoading) return <PageLoading />
  if (topic.error instanceof HttpError && topic.error.status === 404) return <TopicNotFound />
  if (topic.isError || !topic.data) return <ErrorState onRetry={() => topic.refetch()} />
  const actions: TopicActions = { addMaterial: () => setDialog('material') }
  return (
    <div className="topic-page">
      <nav className="breadcrumb" aria-label="Путь">
        <Link to="/">Темы</Link>
        <span aria-hidden="true">/</span>
        <span>{topic.data.title}</span>
      </nav>
      <TopicHeader topic={topic.data} onAddMaterial={actions.addMaterial} onEdit={() => setDialog('edit')} />
      <TopicTabs />
      <div className="topic-content">
        <Outlet context={actions} />
      </div>
      {dialog === 'edit' && <EditTopicModal topic={topic.data} close={close} onDeleted={onDeleted} />}
      {dialog === 'material' && <AddMaterialModal topicId={topicId} close={close} />}
    </div>
  )
}

/** Тема не найдена: её удалили или она принадлежит другому пользователю — backend в обоих случаях отвечает 404. */
function TopicNotFound() {
  return (
    <ErrorState
      title="Тема не найдена"
      message="Возможно, её удалили. Вернитесь к обзору и выберите другую тему."
      action={
        <Link className="button button-primary" to="/">
          К обзору
        </Link>
      }
    />
  )
}

/** Вкладки экрана темы; выбранная подчёркнута. */
function TopicTabs() {
  return (
    <nav className="topic-tabs" aria-label="Разделы темы">
      <NavLink to="materials">Материалы</NavLink>
      <NavLink to="questions">Вопросы</NavLink>
      <NavLink to="cards">Карточки</NavLink>
      <NavLink to="progress">Прогресс</NavLink>
    </nav>
  )
}
