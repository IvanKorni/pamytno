import type { ReactNode } from 'react'
import { BookOpen, FileText, Upload, Youtube } from 'lucide-react'
import type { SourceType } from '../model/types'

/** Типы материалов в порядке показа: значение, подпись и иконка. */
const MATERIAL_TYPES: [SourceType, string, ReactNode][] = [
  ['TEXT', 'Текст', <FileText key="text" size={18} />],
  ['WORD_LIST', 'Слова', <BookOpen key="words" size={18} />],
  ['YOUTUBE', 'YouTube', <Youtube key="youtube" size={18} />],
  ['PDF', 'PDF', <Upload key="pdf" size={18} />],
]

/** Переключатель типа добавляемого материала. */
export function MaterialTypePicker({ kind, onChange }: { kind: SourceType; onChange: (kind: SourceType) => void }) {
  return (
    <div className="material-types">
      {MATERIAL_TYPES.map(([value, label, icon]) => (
        <button
          key={value}
          className={kind === value ? 'material-type active' : 'material-type'}
          onClick={() => onChange(value)}
        >
          {icon}
          {label}
        </button>
      ))}
    </div>
  )
}
