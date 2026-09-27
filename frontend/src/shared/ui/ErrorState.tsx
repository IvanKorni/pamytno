/** Свойства состояния ошибки. */
interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
}

/** Состояние ошибки загрузки с кнопкой повтора, если повтор имеет смысл. */
export function ErrorState({
  title = 'Что-то пошло не так',
  message = 'Не удалось загрузить данные.',
  onRetry,
}: ErrorStateProps) {
  return (
    <div className="empty-state error-state">
      <div className="empty-icon">!</div>
      <h3>{title}</h3>
      <p className="muted">{message}</p>
      {onRetry && (
        <button className="button button-secondary" onClick={onRetry}>
          Попробовать снова
        </button>
      )}
    </div>
  )
}
