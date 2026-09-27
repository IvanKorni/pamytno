import { useState } from 'react'
import { Trash2 } from 'lucide-react'
import { InlineError, Modal } from '@/shared'
import { useDeleteTopic, useUpdateTopic } from '../api/topicQueries'
import type { Topic } from '../model/types'

/** Свойства окна настроек темы. */
interface EditTopicModalProps {
  topic: Topic
  close: () => void
  onDeleted: () => void
}

/** Окно настроек темы: название, описание и удаление. */
export function EditTopicModal({ topic, close, onDeleted }: EditTopicModalProps) {
  const [title, setTitle] = useState(topic.title)
  const [description, setDescription] = useState(topic.description || '')
  const update = useUpdateTopic(topic.id, close)
  const remove = useDeleteTopic(topic.id, () => {
    onDeleted()
    close()
  })
  const error = update.error ?? remove.error
  const confirmRemove = () => window.confirm('Удалить тему вместе с материалами и карточками?') && remove.mutate()
  return (
    <Modal title="Настройки темы" close={close}>
      {error && <InlineError message={error.message} />}
      <div className="form-stack">
        <label>
          Название
          <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={200} required />
        </label>
        <label>
          Описание
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} maxLength={2000} />
        </label>
      </div>
      <div className="modal-danger">
        <button className="text-button danger" disabled={remove.isPending} onClick={confirmRemove}>
          <Trash2 size={15} /> Удалить тему
        </button>
      </div>
      <div className="modal-actions">
        <button className="button button-secondary" onClick={close}>
          Отмена
        </button>
        <button
          className="button button-primary"
          disabled={update.isPending || !title.trim()}
          onClick={() => update.mutate({ title: title.trim(), description: description.trim() })}
        >
          {update.isPending ? 'Сохраняем…' : 'Сохранить'}
        </button>
      </div>
    </Modal>
  )
}
