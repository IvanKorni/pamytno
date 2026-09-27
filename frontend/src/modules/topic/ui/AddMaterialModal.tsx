import { useState } from 'react'
import { InlineError, Modal, useToast } from '@/shared'
import { useAddMaterial } from '../api/sourceQueries'
import { EMPTY_DRAFT, isDraftReady, type MaterialDraft } from '../model/materialDraft'
import { MaterialFields } from './MaterialFields'
import { MaterialTypePicker } from './MaterialTypePicker'

/** Окно добавления материала: текст, список слов, YouTube или PDF. */
export function AddMaterialModal({ topicId, close }: { topicId: string; close: () => void }) {
  const showToast = useToast((state) => state.show)
  const [draft, setDraft] = useState<MaterialDraft>(EMPTY_DRAFT)
  const update = (patch: Partial<MaterialDraft>) => setDraft((current) => ({ ...current, ...patch }))
  const mutation = useAddMaterial(topicId, () => {
    showToast('Материал добавлен')
    close()
  })
  return (
    <Modal title="Добавить материал" close={close}>
      <MaterialTypePicker kind={draft.kind} onChange={(kind) => update({ kind })} />
      {mutation.error && <InlineError message={mutation.error.message} />}
      <MaterialFields draft={draft} update={update} />
      <div className="modal-actions">
        <button className="button button-secondary" onClick={close}>
          Отмена
        </button>
        <button
          className="button button-primary"
          disabled={mutation.isPending || !isDraftReady(draft)}
          onClick={() => mutation.mutate(draft)}
        >
          {mutation.isPending ? 'Добавляем…' : 'Добавить материал'}
        </button>
      </div>
    </Modal>
  )
}
