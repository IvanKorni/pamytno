import { useEffect, useRef } from 'react'

/** Действия по клавишам: ключ — `KeyboardEvent.key` (`' '` для пробела, `'ArrowLeft'`, …). */
export type Shortcuts = Partial<Record<string, () => void>>

/** Элементы, у которых клавиши уже заняты: поля ввода и кнопки обрабатывают их сами. */
const INTERACTIVE = 'input, textarea, select, button, a, [contenteditable="true"], [role="dialog"] *'

/**
 * Горячие клавиши экрана. Срабатывают, только когда фокус не на поле, кнопке или в окне:
 * иначе пробел одновременно нажал бы кнопку в фокусе и выполнил действие экрана.
 */
export function useKeyboardShortcuts(shortcuts: Shortcuts) {
  const current = useRef(shortcuts)
  useEffect(() => {
    current.current = shortcuts
  })
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      const action = current.current[event.key]
      if (!action || event.repeat || isBusyTarget(event)) return
      event.preventDefault()
      action()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [])
}

/** Клавиша нажата с модификатором или в элементе, который обрабатывает её сам. */
function isBusyTarget(event: KeyboardEvent): boolean {
  if (event.ctrlKey || event.metaKey || event.altKey) return true
  return event.target instanceof Element && event.target.matches(INTERACTIVE)
}
