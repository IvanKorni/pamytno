import { sourceTypeLabel, sourceTypeShort } from '../model/labels'
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
      <span className="source-type">{sourceTypeShort(source.type)}</span>
      <div className="source-main">
        <div className="source-title">{source.originalName || source.originalUrl || sourceTypeLabel(source.type)}</div>
        <div className="source-subtitle">{sourceSubtitle(source)}</div>
        {source.status === 'ERROR' && (
          <div className="source-error">{source.errorMessage || 'Не удалось обработать материал.'}</div>
        )}
      </div>
      <span className={`source-status source-status-${source.status.toLowerCase()}`}>
        <SourceStatus source={source} />
      </span>
      <button className="link-button" onClick={onDelete} aria-label="Удалить материал">
        Удалить
      </button>
    </article>
  )
}

/** Статус обработки источника. */
function SourceStatus({ source }: { source: Source }) {
  if (source.status === 'READY') return <>Готово</>
  if (source.status === 'ERROR') return <>Ошибка</>
  return (
    <>
      Обрабатываем<span className="ellipsis">…</span>
    </>
  )
}

/** Подзаголовок источника: ссылка на видео или тип и длина извлечённого текста. */
function sourceSubtitle(source: Source): string {
  const title = source.type === 'YOUTUBE' && source.originalUrl ? source.originalUrl : sourceTypeLabel(source.type)
  const length = source.extractedTextLength ? ` · ${source.extractedTextLength.toLocaleString('ru-RU')} знаков` : ''
  return title + length
}
