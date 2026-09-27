import { InlineError, InlineLoading, Modal } from '@/shared'
import { useTopicContent } from '../api/sourceQueries'

/** Окно с единым текстом темы. */
export function TopicContentModal({ topicId, close }: { topicId: string; close: () => void }) {
  const content = useTopicContent(topicId, true)
  return (
    <Modal title="Исходный текст" close={close} wide>
      <div className="content-reader">
        {content.isLoading && <InlineLoading />}
        {content.isError && <InlineError message="Единый текст ещё не готов. Попробуйте чуть позже." />}
        {content.data && (
          <>
            <div className="reader-meta">
              Версия {content.data.version} · {content.data.content.length.toLocaleString('ru-RU')} символов
            </div>
            <p>{content.data.content}</p>
          </>
        )}
      </div>
    </Modal>
  )
}
