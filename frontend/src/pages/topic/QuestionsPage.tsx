import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Sparkles } from 'lucide-react'
import { QuestionList, QuestionReview, useQuestionsWorkflow } from '@/modules/deck'
import { learningKeys } from '@/modules/learning'
import { EmptyState, ErrorState, InlineError, InlineLoading, ProcessingCard } from '@/shared'
import { CardsReadyBanner, QuestionsFooter } from './QuestionsFooter'
import { useTopicId } from './useTopicId'

/** Режим отбора вопросов: по одному или списком. */
type View = 'review' | 'list'

/** Сценарий отбора вопросов, который возвращает `useQuestionsWorkflow`. */
type Workflow = ReturnType<typeof useQuestionsWorkflow>

/** Экран вопросов: генерация, отбор по одному или списком и создание карточек. */
export function QuestionsPage() {
  const topicId = useTopicId()
  const queryClient = useQueryClient()
  const [view, setView] = useState<View>('review')
  const workflow = useQuestionsWorkflow(topicId, () =>
    queryClient.invalidateQueries({ queryKey: learningKeys.progress(topicId) }),
  )
  if (workflow.questions.isLoading) return <InlineLoading />
  if (workflow.questions.isError) return <ErrorState onRetry={() => workflow.questions.refetch()} />
  const all = workflow.questions.data || []
  const approved = all.filter((item) => item.status === 'APPROVED')
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
      {all.length > 0 && !workflow.processingQuestions && <QuestionsFooter workflow={workflow} approved={approved.length} />}
      {workflow.processingCards && (
        <ProcessingCard
          title="Создаём карточки"
          subtitle="Превращаем выбранные вопросы в карточки…"
          count={workflow.cardJob.data?.itemsCreated}
        />
      )}
      {approved.length > 0 && !workflow.processingCards && <CardsReadyBanner count={approved.length} />}
      {!all.length && !workflow.processingQuestions && (
        <button className="text-button centered-action" onClick={() => workflow.generate.mutate()}>
          <Sparkles size={15} /> Создать вопросы
        </button>
      )}
    </section>
  )
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
function QuestionsBody({ workflow, view }: { workflow: Workflow; view: View }) {
  const all = workflow.questions.data || []
  const pending = all.filter((item) => item.status === 'GENERATED')
  if (!all.length && !workflow.processingQuestions) {
    return (
      <EmptyState
        icon={<Sparkles size={24} />}
        title="Вопросов пока нет"
        text="Когда материалы будут готовы, запустите генерацию вопросов."
        action={
          <button className="button button-primary" onClick={() => workflow.generate.mutate()}>
            <Sparkles size={17} /> Создать вопросы
          </button>
        }
      />
    )
  }
  if (workflow.processingQuestions) {
    return (
      <ProcessingCard
        title="Анализируем материалы"
        subtitle="Создаём вопросы по единому тексту…"
        count={workflow.job.data?.itemsCreated}
      />
    )
  }
  if (view === 'list') {
    return <QuestionList questions={all} selected={workflow.selected} setSelected={workflow.setSelected} />
  }
  const current = pending[0]
  return (
    <QuestionReview
      pending={pending}
      current={current}
      index={0}
      onDecide={(decision) => current && workflow.decide.mutate({ id: current.id, decision })}
    />
  )
}
