import { BookOpen, Check, FileText, Trash2, Youtube } from 'lucide-react'
import { sourceTypeLabel } from '../model/labels'
import type { Source } from '../model/types'

/** Свойства строки источника. */
interface SourceRowProps {
  source: Source
  onDelete: () => void
}

/** Строка источника: тип, название, объём текста, статус обработки и удаление. */
export function SourceRow({ source, onDelete }: SourceRowProps) {
  return (
    <article className="source-row">
      <div className={`source-icon source-${source.type.toLowerCase()}`}>
        <SourceIcon source={source} />
      </div>
      <div className="source-main">
        <div className="source-title">{source.originalName || source.originalUrl || source.type}</div>
        <div className="source-subtitle">{sourceSubtitle(source)}</div>
        {source.status === 'ERROR' && (
          <div className="source-error">{source.errorMessage || 'Не удалось обработать материал.'}</div>
        )}
      </div>
      <div className={`source-status source-status-${source.status.toLowerCase()}`}>
        <SourceStatus source={source} />
      </div>
      <button className="icon-button subtle" onClick={onDelete} title="Удалить материал" aria-label="Удалить материал">
        <Trash2 size={16} />
      </button>
    </article>
  )
}

/** Иконка типа источника. */
function SourceIcon({ source }: { source: Source }) {
  if (source.type === 'PDF') return <FileText size={20} />
  if (source.type === 'YOUTUBE') return <Youtube size={20} />
  return <BookOpen size={20} />
}

/** Статус обработки источника. */
function SourceStatus({ source }: { source: Source }) {
  if (source.status === 'READY') {
    return (
      <>
        <Check size={14} /> Готово
      </>
    )
  }
  if (source.status === 'ERROR') return <>Ошибка</>
  return (
    <>
      <span className="mini-spinner" /> Обрабатываем
    </>
  )
}

/** Подзаголовок источника: ссылка на видео или тип и длина извлечённого текста. */
function sourceSubtitle(source: Source): string {
  const title = source.type === 'YOUTUBE' && source.originalUrl ? source.originalUrl : sourceTypeLabel(source.type)
  const length = source.extractedTextLength ? ` · ${source.extractedTextLength.toLocaleString('ru-RU')} знаков` : ''
  return title + length
}
