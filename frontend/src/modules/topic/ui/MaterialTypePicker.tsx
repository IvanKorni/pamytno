import type { SourceType } from '../model/types'

/** Типы материалов в порядке показа: значение и подпись вкладки. */
const MATERIAL_TYPES: [SourceType, string][] = [
  ['TEXT', 'Текст'],
  ['WORD_LIST', 'Слова'],
  ['YOUTUBE', 'YouTube'],
  ['PDF', 'PDF'],
]

/** Вкладки типа добавляемого материала. */
export function MaterialTypePicker({ kind, onChange }: { kind: SourceType; onChange: (kind: SourceType) => void }) {
  return (
    <div className="material-types" role="group" aria-label="Тип материала">
      {MATERIAL_TYPES.map(([value, label]) => (
        <button key={value} className="material-type" aria-pressed={kind === value} onClick={() => onChange(value)}>
          {label}
        </button>
      ))}
    </div>
  )
}
