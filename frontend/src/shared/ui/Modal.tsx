import { useEffect, useId, useRef, type ReactNode, type RefObject } from 'react'
import { X } from 'lucide-react'

/** Свойства модального окна. */
interface ModalProps {
  title: string
  close: () => void
  children: ReactNode
  wide?: boolean
}

/**
 * Модальное окно с заголовком. Закрывается крестиком, клавишей Esc и кликом по подложке;
 * при открытии забирает фокус, при закрытии возвращает его туда, где он был.
 */
export function Modal({ title, close, children, wide = false }: ModalProps) {
  const titleId = useId()
  const dialog = useRef<HTMLDivElement>(null)
  useFocusInside(dialog)
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') close()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [close])
  return (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) close()
      }}
    >
      <div
        ref={dialog}
        className={`modal ${wide ? 'modal-wide' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
      >
        <div className="modal-header">
          <h2 id={titleId}>{title}</h2>
          <button className="icon-button" onClick={close} aria-label="Закрыть">
            <X size={19} />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

/** Переносит фокус в окно при открытии, если он ещё не внутри, и возвращает прежний фокус при закрытии. */
function useFocusInside(dialog: RefObject<HTMLDivElement | null>) {
  useEffect(() => {
    const previous = document.activeElement instanceof HTMLElement ? document.activeElement : null
    if (dialog.current && !dialog.current.contains(document.activeElement)) dialog.current.focus()
    return () => previous?.focus()
  }, [dialog])
}
