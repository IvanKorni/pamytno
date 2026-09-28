import { useState } from 'react'
import { InlineError, Modal } from '@/shared'
import { useCreateTopic } from '../api/topicQueries'

/** Свойства окна новой темы. */
interface CreateTopicModalProps {
  close: () => void
  onCreated: (topicId: string) => void
}

/** Окно новой темы: название и необязательное описание; куда перейти после создания, решает страница. */
export function CreateTopicModal({ close, onCreated }: CreateTopicModalProps) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const create = useCreateTopic((topic) => onCreated(topic.id))
  return (
    <Modal title="Новая тема" close={close}>
      <form
        className="form-stack"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate({ title: title.trim(), description: description.trim() })
        }}
      >
        {create.error && <InlineError message={create.error.message} />}
        <label>
          Название
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Например, Kafka"
            maxLength={200}
            autoFocus
            required
          />
        </label>
        <label>
          <span>
            Описание <span className="optional">— необязательно</span>
          </span>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} maxLength={2000} />
        </label>
        <div>
          <button className="button button-primary" disabled={create.isPending || !title.trim()}>
            {create.isPending ? 'Создаём…' : 'Создать тему'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
