import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { AddWordsModal, useVocabularyGeneration } from '@/modules/deck'
import { learningKeys, useRefreshProgress } from '@/modules/learning'
import { AddMaterialModal, EditTopicModal, useTopic, type Topic } from '@/modules/topic'
import { ErrorState, HttpError, PageLoading } from '@/shared'
import type { TopicActions } from './topicActions'
import { TopicHeader } from './TopicHeader'
import { useTopicId } from './useTopicId'

/** Какое окно темы открыто. */
type Dialog = 'edit' | 'material' | 'words'

/** Каркас экранов темы: путь, шапка с действиями, вкладки и вложенный экран. */
export function TopicLayout() {
  const topicId = useTopicId()
  const topic = useTopic(topicId)
  const words = useVocabularyGeneration(topicId, useRefreshProgress(topicId))
  const [dialog, setDialog] = useState<Dialog>()
  if (topic.isLoading) return <PageLoading />
  if (topic.error instanceof HttpError && topic.error.status === 404) return <TopicNotFound />
  if (topic.isError || !topic.data) return <ErrorState onRetry={() => topic.refetch()} />
  const actions: TopicActions = {
    addMaterial: () => setDialog('material'),
    addWords: () => {
      words.resetLaunch()
      setDialog('words')
    },
    words,
  }
  return (
    <div className="topic-page">
      <nav className="breadcrumb" aria-label="Путь">
        <Link to="/">Темы</Link>
        <span aria-hidden="true">/</span>
        <span>{topic.data.title}</span>
      </nav>
      <TopicHeader
        topic={topic.data}
        onAddMaterial={actions.addMaterial}
        onAddWords={actions.addWords}
        onEdit={() => setDialog('edit')}
      />
      <TopicTabs />
      <div className="topic-content">
        <Outlet context={actions} />
      </div>
      <TopicDialog dialog={dialog} topic={topic.data} words={words} close={() => setDialog(undefined)} />
    </div>
  )
}

/** Свойства открытого окна темы. */
interface TopicDialogProps {
  dialog?: Dialog
  topic: Topic
  words: TopicActions['words']
  close: () => void
}

/**
 * Открытое окно темы: настройки, материал или слова. После удаления темы — обзор,
 * запущенные слова ведут на вкладку карточек.
 */
function TopicDialog({ dialog, topic, words, close }: TopicDialogProps) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const onDeleted = () => {
    queryClient.invalidateQueries({ queryKey: learningKeys.dashboard })
    navigate('/')
  }
  const onWordsStarted = () => {
    close()
    navigate(`/topics/${topic.id}/cards`)
  }
  if (dialog === 'edit') return <EditTopicModal topic={topic} close={close} onDeleted={onDeleted} />
  if (dialog === 'material') return <AddMaterialModal topicId={topic.id} close={close} />
  if (dialog === 'words') return <AddWordsModal run={words} close={close} onStarted={onWordsStarted} />
  return null
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
