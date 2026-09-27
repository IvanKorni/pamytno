import { ProgressOverview, useTopicProgress } from '@/modules/learning'
import { ErrorState, InlineLoading, StatCard } from '@/shared'
import { useTopicId } from './useTopicId'

/** Экран прогресса темы: доля изученного и сколько карточек ждут повторения. */
export function ProgressPage() {
  const progress = useTopicProgress(useTopicId())
  if (progress.isLoading) return <InlineLoading />
  if (progress.isError || !progress.data) return <ErrorState onRetry={() => progress.refetch()} />
  const data = progress.data
  return (
    <section className="topic-section">
      <div className="section-heading">
        <div>
          <h2>Прогресс</h2>
          <p className="muted">Понятная картина того, что уже закрепилось.</p>
        </div>
      </div>
      <ProgressOverview progress={data} />
      <div className="progress-grid">
        <StatCard label="Всего карточек" value={data.totalCards} />
        <StatCard label="Повторить сейчас" value={data.dueCards} accent />
        <StatCard label="До конца сегодня" value={data.dueToday} />
      </div>
    </section>
  )
}
