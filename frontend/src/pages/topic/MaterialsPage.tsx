import { useState } from 'react'
import { FileText, Plus, Upload } from 'lucide-react'
import {
  AddMaterialModal,
  SourceRow,
  TopicContentModal,
  useDeleteSource,
  useSources,
  type Source,
} from '@/modules/topic'
import { EmptyState, ErrorState, InlineLoading } from '@/shared'
import { useTopicId } from './useTopicId'

/** Экран материалов темы: источники, статус их обработки и единый текст. */
export function MaterialsPage() {
  const topicId = useTopicId()
  const [adding, setAdding] = useState(false)
  const [contentOpen, setContentOpen] = useState(false)
  const sources = useSources(topicId)
  const hasReady = sources.data?.some((source) => source.status === 'READY')
  return (
    <section className="topic-section">
      <div className="section-heading">
        <div>
          <h2>Материалы</h2>
          <p className="muted">Соберите всё, что помогает разобраться в теме.</p>
        </div>
        <button className="button button-primary" onClick={() => setAdding(true)}>
          <Plus size={17} /> Добавить материал
        </button>
      </div>
      {sources.isLoading && <InlineLoading />}
      {sources.isError && <ErrorState onRetry={() => sources.refetch()} />}
      {sources.data && <SourceList topicId={topicId} sources={sources.data} onAdd={() => setAdding(true)} />}
      {hasReady && <MasterTextCard onOpen={() => setContentOpen(true)} />}
      {contentOpen && <TopicContentModal topicId={topicId} close={() => setContentOpen(false)} />}
      {adding && <AddMaterialModal topicId={topicId} close={() => setAdding(false)} />}
    </section>
  )
}

/** Свойства списка источников. */
interface SourceListProps {
  topicId: string
  sources: Source[]
  onAdd: () => void
}

/** Список источников с удалением или приглашение добавить первый. */
function SourceList({ topicId, sources, onAdd }: SourceListProps) {
  const remove = useDeleteSource(topicId)
  if (!sources.length) {
    return (
      <EmptyState
        icon={<FileText size={24} />}
        title="Добавьте первый материал"
        text="Подойдёт PDF, ссылка на YouTube или собственные заметки."
        action={
          <button className="button button-primary" onClick={onAdd}>
            <Upload size={17} /> Добавить материал
          </button>
        }
      />
    )
  }
  const confirmRemove = (id: string) => window.confirm('Удалить этот материал?') && remove.mutate(id)
  return (
    <div className="source-list">
      {sources.map((source) => (
        <SourceRow key={source.id} source={source} onDelete={() => confirmRemove(source.id)} />
      ))}
    </div>
  )
}

/** Карточка единого текста темы. */
function MasterTextCard({ onOpen }: { onOpen: () => void }) {
  return (
    <div className="master-text-card">
      <div>
        <span className="eyebrow">Единый текст темы</span>
        <h3>Все материалы собраны в одном месте</h3>
        <p className="muted">Откройте исходный текст или переходите к созданию вопросов.</p>
      </div>
      <button className="button button-secondary" onClick={onOpen}>
        Открыть текст
      </button>
    </div>
  )
}
