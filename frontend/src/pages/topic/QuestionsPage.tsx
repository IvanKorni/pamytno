import { useState } from 'react'
import { QuestionList, QuestionReview, useQuestionsWorkflow, type QuestionsWorkflow } from '@/modules/deck'
import { useRefreshProgress } from '@/modules/learning'
import { EmptyState, ErrorState, InlineError, InlineLoading, ProcessingCard, Segmented } from '@/shared'
import { CardsReady, QuestionsFooter } from './QuestionsFooter'
import { useTopicId } from './useTopicId'

/** Режим отбора вопросов: по одному или списком. */
type View = 'review' | 'list'

/** Варианты переключателя режима отбора. */
const VIEWS = [
  { value: 'review' as const, label: 'По одному' },
  { value: 'list' as const, label: 'Списком' },
]

/** Экран вопросов: загрузка, ход генерации карточек и её итог или отбор вопросов. */
export function QuestionsPage() {
  const topicId = useTopicId()
  const refreshProgress = useRefreshProgress(topicId)
  const workflow = useQuestionsWorkflow(topicId, refreshProgress)
  const [seenJob, setSeenJob] = useState<string>()
  const { questions, cardRun } = workflow
  if (questions.isLoading) return <InlineLoading />
  if (questions.isError) return <ErrorState onRetry={() => questions.refetch()} />
  if (cardRun.running) {
    return <ProcessingCard title="Создаём карточки" subtitle="можно уйти с экрана — карточки создадутся" />
  }
  const readyJob = cardRun.readyJob
  if (readyJob && readyJob.id !== seenJob) {
    return <CardsReady topicId={topicId} count={readyJob.itemsCreated} onBack={() => setSeenJob(readyJob.id)} />
  }
  return <QuestionsSection workflow={workflow} />
}

/** Отбор вопросов: переключатель режима, генерация или вопросы и панель действий под ними. */
function QuestionsSection({ workflow }: { workflow: QuestionsWorkflow }) {
  const [view, setView] = useState<View>('review')
  const ready = Boolean(workflow.questions.data?.length) && !workflow.questionRun.running
  return (
    <section>
      {workflow.error && <InlineError message={workflow.error} />}
      {ready && (
        <div className="questions-toolbar">
          <Segmented label="Режим отбора" options={VIEWS} value={view} onChange={setView} />
        </div>
      )}
      <QuestionsBody workflow={workflow} view={view} />
      {ready && <QuestionsFooter workflow={workflow} />}
    </section>
  )
}

/** Основной блок: приглашение сгенерировать, ход генерации или отбор вопросов. */
function QuestionsBody({ workflow, view }: { workflow: QuestionsWorkflow; view: View }) {
  const all = workflow.questions.data ?? []
  if (workflow.questionRun.running) {
    return (
      <ProcessingCard
        title="Анализируем материалы"
        subtitle="обычно меньше минуты · можно уйти с экрана"
        count={workflow.questionRun.itemsCreated}
      />
    )
  }
  if (!all.length) {
    return (
      <EmptyState
        title="Вопросов пока нет."
        text="Когда материалы будут готовы, создайте по ним вопросы."
        action={
          <button className="button button-primary" onClick={() => workflow.questionRun.launch()}>
            Создать вопросы
          </button>
        }
      />
    )
  }
  if (view === 'list') {
    return <QuestionList questions={all} selected={workflow.selected} setSelected={workflow.setSelected} />
  }
  return <ReviewOneByOne workflow={workflow} />
}

/** Отбор вопросов по одному: номер вопроса считается среди ещё не превращённых в карточки. */
function ReviewOneByOne({ workflow }: { workflow: QuestionsWorkflow }) {
  const reviewable = (workflow.questions.data ?? []).filter((item) => item.status !== 'CARD_CREATED')
  const pending = reviewable.filter((item) => item.status === 'GENERATED')
  const current = pending[0]
  return (
    <QuestionReview
      current={current}
      position={reviewable.length - pending.length + 1}
      total={reviewable.length}
      deciding={workflow.deciding}
      onDecide={(decision) => current && workflow.decide(current.id, decision)}
    />
  )
}
