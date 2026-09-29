import type { ClipboardEvent } from 'react'
import { markBoldText } from '../model/pastedText'
import { MAX_WORDS_TEXT, modeInfo, WORDS_MODES, type WordsDraft, type WordsMode } from '../model/wordsDraft'

/** Свойства полей окна карточек слов. */
interface WordsFieldsProps {
  draft: WordsDraft
  update: (patch: Partial<WordsDraft>) => void
}

/** Вкладки «что взять из текста». */
export function WordsModePicker({ mode, onChange }: { mode: WordsMode; onChange: (mode: WordsMode) => void }) {
  return (
    <div className="material-types" role="group" aria-label="Что взять из текста">
      {WORDS_MODES.map((info) => (
        <button
          key={info.mode}
          className="material-type"
          aria-pressed={mode === info.mode}
          onClick={() => onChange(info.mode)}
        >
          {info.label}
        </button>
      ))}
    </div>
  )
}

/** Своя инструкция (для этого режима) и поле слов или текста с подсказкой режима. */
export function WordsFields({ draft, update }: WordsFieldsProps) {
  const info = modeInfo(draft.mode)
  return (
    <div className="form-stack">
      {draft.mode === 'CUSTOM' && (
        <label>
          Что взять из текста
          <input
            value={draft.customInstruction}
            onChange={(e) => update({ customInstruction: e.target.value })}
            placeholder="Например: только фразовые глаголы"
            maxLength={500}
          />
        </label>
      )}
      <label>
        Слова или текст
        <textarea
          value={draft.text}
          onChange={(e) => update({ text: e.target.value })}
          onPaste={(e) => pasteWithBold(e, (text) => update({ text }))}
          rows={9}
          placeholder={info.placeholder}
          maxLength={MAX_WORDS_TEXT}
          className="words-field"
          aria-describedby="words-hint"
          required
        />
      </label>
      <p id="words-hint" className="field-hint">
        {info.hint}
      </p>
    </div>
  )
}

/**
 * Вставка с сохранением жирного: если в буфере HTML с выделением, вместо выделенного текста вставляется
 * тот же текст с `**слово**`. Без выделения браузер вставляет текст сам.
 */
function pasteWithBold(event: ClipboardEvent<HTMLTextAreaElement>, setText: (text: string) => void) {
  const marked = markBoldText(event.clipboardData.getData('text/html'))
  if (marked === undefined) return
  event.preventDefault()
  const { value, selectionStart, selectionEnd } = event.currentTarget
  setText((value.slice(0, selectionStart) + marked + value.slice(selectionEnd)).slice(0, MAX_WORDS_TEXT))
}
