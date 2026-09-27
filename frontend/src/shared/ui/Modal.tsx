import type { ReactNode } from 'react'
import { X } from 'lucide-react'

/** Свойства модального окна. */
interface ModalProps {
  title: string
  close: () => void
  children: ReactNode
  wide?: boolean
}

/** Модальное окно с заголовком; закрывается крестиком и кликом по подложке. */
export function Modal({ title, close, children, wide = false }: ModalProps) {
  return (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) close()
      }}
    >
      <div className={`modal ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true">
        <div className="modal-header">
          <h2>{title}</h2>
          <button className="icon-button" onClick={close} aria-label="Закрыть">
            <X size={19} />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
