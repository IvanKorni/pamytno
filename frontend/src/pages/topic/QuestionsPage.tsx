import { useState } from 'react'
import { Sparkles } from 'lucide-react'
import { QuestionList, QuestionReview, useQuestionsWorkflow, type QuestionsWorkflow } from '@/modules/deck'
import { useRefreshProgress } from '@/modules/learning'
import { EmptyState, ErrorState, InlineError, InlineLoading, ProcessingCard } from '@/shared'
import { CardsReadyBanner, QuestionsFooter } from './QuestionsFooter'
import { useTopicId } from './useTopicId'

/** Режим отбора вопросов: по одному или списком. */
type View = 'review' | 'list'

/** Экран вопросов: генерация, отбор по одному или списком и создание карточек. */
export function QuestionsPage() {
  const topicId = useTopicId()
  const [view, setView] = useState<View>('review')
  const refreshProgress = useRefreshProgress(topicId)
  const workflow = useQuestionsWorkflow(topicId, refreshProgress)
  const { questions, questionRun, cardRun } = workflow
  if (questions.isLoading) return <InlineLoading />
  if (questions.isError) return <ErrorState onRetry={() => questions.refetch()} />
  const hasQuestions = Boolean(questions.data?.length)
  return (
    <section className="topic-section">
      <div className="section-heading">
        <div>
          <h2>Вопросы</h2>
          <p className="muted">Оставьте только то, что действительно хотите помнить.</p>
        </div>
        <ViewSwitch view={view} setView={setView} />
      </div>
      {workflow.error && <InlineError message={workflow.error} />}
      <QuestionsBody workflow={workflow} view={view} />
      {hasQuestions && !questionRun.running && <QuestionsFooter workflow={workflow} />}
      <CardGenerationStatus topicId={topicId} run={cardRun} />
    </section>
  )
}

/** Ход генерации карточек или баннер с созданными карточками. */
function CardGenerationStatus({ topicId, run }: { topicId: string; run: QuestionsWorkflow['cardRun'] }) {
  if (run.running) {
    return (
      <ProcessingCard
        title="Создаём карточки"
        subtitle="Превращаем выбранные вопросы в карточки…"
        count={run.itemsCreated}
      />
    )
  }
  return run.readyJob ? <CardsReadyBanner topicId={topicId} count={run.readyJob.itemsCreated} /> : null
}

/** Переключатель режима отбора. */
function ViewSwitch({ view, setView }: { view: View; setView: (view: View) => void }) {
  return (
    <div className="heading-actions">
      <button className={view === 'review' ? 'segmented active' : 'segmented'} onClick={() => setView('review')}>
        По одному
      </button>
      <button className={view === 'list' ? 'segmented active' : 'segmented'} onClick={() => setView('list')}>
        Списком
      </button>
    </div>
  )
}

/** Основной блок: приглашение сгенерировать, ход генерации или отбор вопросов. */
function QuestionsBody({ workflow, view }: { workflow: QuestionsWorkflow; view: View }) {
  const all = workflow.questions.data ?? []
  if (workflow.questionRun.running) {
    return (
      <ProcessingCard
        title="Анализируем материалы"
        subtitle="Создаём вопросы по единому тексту…"
        count={workflow.questionRun.itemsCreated}
      />
    )
  }
  if (!all.length) {
    return (
      <EmptyState
        icon={<Sparkles size={24} />}
        title="Вопросов пока нет"
        text="Когда материалы будут готовы, запустите генерацию вопросов."
        action={
          <button className="button button-primary" onClick={workflow.questionRun.launch}>
            <Sparkles size={17} /> Создать вопросы
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
