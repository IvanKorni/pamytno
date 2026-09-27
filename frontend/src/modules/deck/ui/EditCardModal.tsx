import { useState } from 'react'
import { InlineError, Modal, useToast } from '@/shared'
import { useUpdateCard } from '../api/cardQueries'
import type { Flashcard } from '../model/types'

/** Окно изменения вопроса и ответа карточки. */
export function EditCardModal({ card, close }: { card: Flashcard; close: () => void }) {
  const showToast = useToast((state) => state.show)
  const [front, setFront] = useState(card.front)
  const [back, setBack] = useState(card.back)
  const mutation = useUpdateCard(card, () => {
    showToast('Карточка сохранена')
    close()
  })
  return (
    <Modal title="Изменить карточку" close={close}>
      {mutation.error && <InlineError message={mutation.error.message} />}
      <div className="form-stack">
        <label>
          Вопрос
          <textarea value={front} onChange={(e) => setFront(e.target.value)} rows={4} maxLength={2000} />
        </label>
        <label>
          Ответ
          <textarea value={back} onChange={(e) => setBack(e.target.value)} rows={7} maxLength={10000} />
        </label>
      </div>
      <div className="modal-actions">
        <button className="button button-secondary" onClick={close}>
          Отмена
        </button>
        <button
          className="button button-primary"
          disabled={mutation.isPending || !front.trim() || !back.trim()}
          onClick={() => mutation.mutate({ front: front.trim(), back: back.trim() })}
        >
          {mutation.isPending ? 'Сохраняем…' : 'Сохранить'}
        </button>
      </div>
    </Modal>
  )
}
