import { Check, Upload } from 'lucide-react'
import type { MaterialDraft } from '../model/materialDraft'

/** Свойства полей черновика материала. */
interface MaterialFieldsProps {
  draft: MaterialDraft
  update: (patch: Partial<MaterialDraft>) => void
}

/** Поля формы для выбранного типа материала. */
export function MaterialFields({ draft, update }: MaterialFieldsProps) {
  if (draft.kind === 'YOUTUBE') return <YoutubeFields draft={draft} update={update} />
  if (draft.kind === 'PDF') return <PdfFields draft={draft} update={update} />
  return <TextFields draft={draft} update={update} />
}

/** Ссылка на видео YouTube и название. */
function YoutubeFields({ draft, update }: MaterialFieldsProps) {
  return (
    <div className="form-stack">
      <label>
        YouTube URL
        <input
          value={draft.url}
          onChange={(e) => update({ url: e.target.value })}
          placeholder="https://www.youtube.com/watch?v=…"
          required
        />
      </label>
      <NameField draft={draft} update={update} placeholder="Название видео" />
    </div>
  )
}

/** Выбор PDF-файла и название. */
function PdfFields({ draft, update }: MaterialFieldsProps) {
  return (
    <div className="form-stack">
      <label>
        PDF-файл
        <div className="dropzone">
          <input type="file" accept="application/pdf,.pdf" onChange={(e) => update({ file: e.target.files?.[0] })} />
          {draft.file ? <PickedFile name={draft.file.name} /> : <FilePrompt />}
        </div>
      </label>
      <NameField draft={draft} update={update} placeholder="Название файла" />
    </div>
  )
}

/** Текст заметок или список слов и название. */
function TextFields({ draft, update }: MaterialFieldsProps) {
  const words = draft.kind === 'WORD_LIST'
  return (
    <div className="form-stack">
      <label>
        {words ? 'Список слов' : 'Текст заметок'}
        <textarea
          value={draft.text}
          onChange={(e) => update({ text: e.target.value })}
          rows={9}
          placeholder={words ? 'optimistic locking\nMVCC\npessimistic locking' : 'Вставьте сюда материал…'}
          required
        />
      </label>
      <NameField draft={draft} update={update} placeholder={words ? 'Термины' : 'Мои заметки'} />
    </div>
  )
}

/** Необязательное название материала. */
function NameField({ draft, update, placeholder }: MaterialFieldsProps & { placeholder: string }) {
  return (
    <label>
      Название <span className="optional">необязательно</span>
      <input value={draft.name} onChange={(e) => update({ name: e.target.value })} placeholder={placeholder} />
    </label>
  )
}

/** Выбранный PDF-файл. */
function PickedFile({ name }: { name: string }) {
  return (
    <>
      <Check size={24} />
      <strong>{name}</strong>
      <span className="muted">Файл выбран</span>
    </>
  )
}

/** Приглашение выбрать или перетащить PDF. */
function FilePrompt() {
  return (
    <>
      <Upload size={24} />
      <strong>Перетащите PDF сюда</strong>
      <span className="muted">или выберите файл</span>
    </>
  )
}
