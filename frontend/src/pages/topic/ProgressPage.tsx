import { ProgressOverview, useTopicProgress } from '@/modules/learning'
import { ErrorState, InlineLoading } from '@/shared'
import { useTopicId } from './useTopicId'

/** Экран прогресса темы: доля изученного и сколько карточек ждут повторения. */
export function ProgressPage() {
  const progress = useTopicProgress(useTopicId())
  if (progress.isLoading) return <InlineLoading />
  if (progress.isError || !progress.data) return <ErrorState onRetry={() => progress.refetch()} />
  return <ProgressOverview progress={progress.data} />
}
