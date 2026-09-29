import { useState } from 'react'
import { InlineError, Modal } from '@/shared'
import type { VocabularyRun } from '../model/useVocabularyGeneration'
import { EMPTY_WORDS_DRAFT, isWordsDraftReady, toVocabularyRequest, type WordsDraft } from '../model/wordsDraft'
import { WordsFields, WordsModePicker } from './WordsFields'

/** Свойства окна карточек слов. */
interface AddWordsModalProps {
  run: VocabularyRun
  close: () => void
  onStarted: () => void
}

/**
 * Окно карточек английских слов: слова или текст и что из них взять. Когда backend принял задачу,
 * окно закрывается, а ход составления виден на экране карточек.
 */
export function AddWordsModal({ run, close, onStarted }: AddWordsModalProps) {
  const [draft, setDraft] = useState<WordsDraft>(EMPTY_WORDS_DRAFT)
  const update = (patch: Partial<WordsDraft>) => setDraft((current) => ({ ...current, ...patch }))
  return (
    <Modal title="Карточки слов" close={close}>
      <WordsModePicker mode={draft.mode} onChange={(mode) => update({ mode })} />
      {run.launchError && <InlineError message={run.launchError} />}
      <WordsFields draft={draft} update={update} />
      <div className="modal-actions">
        <span className="mono">спереди — объяснение и пропуск, сзади — слово и перевод</span>
        <button
          className="button button-primary"
          disabled={run.running || !isWordsDraftReady(draft)}
          onClick={() => run.launchThen(onStarted, toVocabularyRequest(draft))}
        >
          {run.running ? 'Составляем…' : 'Составить карточки'}
        </button>
      </div>
    </Modal>
  )
}
