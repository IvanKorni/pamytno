import type { ReactNode } from 'react'

/** Свойства состояния ошибки. */
interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
  action?: ReactNode
}

/** Состояние ошибки загрузки с кнопкой повтора, если повтор имеет смысл, или своим действием. */
export function ErrorState({
  title = 'Что-то пошло не так',
  message = 'Не удалось загрузить данные.',
  onRetry,
  action,
}: ErrorStateProps) {
  return (
    <div className="empty-state error-state">
      <h2>{title}</h2>
      <p>{message}</p>
      {onRetry && (
        <button className="button button-secondary" onClick={onRetry}>
          Попробовать снова
        </button>
      )}
      {action}
    </div>
  )
}

/** Строка с ошибкой внутри формы или блока. */
export function InlineError({ message }: { message: string }) {
  return <div className="inline-error">{message}</div>
}
