/** Вариант переключателя: значение и подпись. */
export interface SegmentedOption<T extends string> {
  value: T
  label: string
}

/** Свойства переключателя режимов. */
interface SegmentedProps<T extends string> {
  options: SegmentedOption<T>[]
  value: T
  onChange: (value: T) => void
  label: string
}

/** Переключатель режимов экрана из соседних кнопок; выбранная кнопка залита и отмечена `aria-pressed`. */
export function Segmented<T extends string>({ options, value, onChange, label }: SegmentedProps<T>) {
  return (
    <div className="segmented" role="group" aria-label={label}>
      {options.map((option) => (
        <button key={option.value} aria-pressed={option.value === value} onClick={() => onChange(option.value)}>
          {option.label}
        </button>
      ))}
    </div>
  )
}
