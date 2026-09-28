import { InlineError, InlineLoading } from '@/shared'
import { useTopicContent } from '../api/sourceQueries'

/** Единый текст темы, собранный из всех материалов, — только для чтения. */
export function TopicContentPanel({ topicId }: { topicId: string }) {
  const content = useTopicContent(topicId, true)
  return (
    <section className="content-panel" aria-label="Исходный текст">
      <div className="label-caps">Исходный текст · только чтение</div>
      {content.isLoading && <InlineLoading />}
      {content.isError && <InlineError message="Единый текст ещё не готов. Попробуйте чуть позже." />}
      {content.data && (
        <>
          <div className="mono content-meta">
            версия {content.data.version} · {content.data.content.length.toLocaleString('ru-RU')} символов
          </div>
          <p className="content-text">{content.data.content}</p>
        </>
      )}
    </section>
  )
}
