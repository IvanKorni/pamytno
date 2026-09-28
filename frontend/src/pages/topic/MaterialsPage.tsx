import { useState } from 'react'
import { SourceRow, TopicContentPanel, useDeleteSource, useSources, type Source } from '@/modules/topic'
import { EmptyState, ErrorState, InlineLoading } from '@/shared'
import { MaterialActions } from './MaterialActions'
import { useTopicActions } from './topicActions'
import { useTopicId } from './useTopicId'

/** Экран материалов темы: источники, статус их обработки, переход к вопросам и единый текст. */
export function MaterialsPage() {
  const topicId = useTopicId()
  const { addMaterial } = useTopicActions()
  const [showContent, setShowContent] = useState(false)
  const sources = useSources(topicId)
  if (sources.isLoading) return <InlineLoading />
  if (sources.isError || !sources.data) return <ErrorState onRetry={() => sources.refetch()} />
  if (!sources.data.length) {
    return (
      <EmptyState
        title="Здесь пока нет материалов."
        text="Добавьте PDF, видео, текст или список слов — из них появятся вопросы."
        action={
          <button className="button button-primary" onClick={addMaterial}>
            Добавить материал
          </button>
        }
      />
    )
  }
  const hasReady = sources.data.some((source) => source.status === 'READY')
  return (
    <section>
      <SourceList topicId={topicId} sources={sources.data} />
      {hasReady && (
        <MaterialActions
          topicId={topicId}
          showContent={showContent}
          toggleContent={() => setShowContent(!showContent)}
        />
      )}
      {showContent && <TopicContentPanel topicId={topicId} />}
    </section>
  )
}

/** Таблица источников с удалением. */
function SourceList({ topicId, sources }: { topicId: string; sources: Source[] }) {
  const remove = useDeleteSource(topicId)
  const confirmRemove = (id: string) => window.confirm('Удалить этот материал?') && remove.mutate(id)
  return (
    <div className="source-list">
      {sources.map((source) => (
        <SourceRow key={source.id} source={source} onDelete={() => confirmRemove(source.id)} />
      ))}
    </div>
  )
}
