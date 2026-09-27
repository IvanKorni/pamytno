import type { ReactNode } from 'react'

/** Свойства пустого состояния. */
interface EmptyStateProps {
  icon?: ReactNode
  title: string
  text: string
  action?: ReactNode
}

/** Пустое состояние экрана: иконка, пояснение и действие, которое поможет начать. */
export function EmptyState({ icon, title, text, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      {icon && <div className="empty-icon">{icon}</div>}
      <h3>{title}</h3>
      <p className="muted">{text}</p>
      {action}
    </div>
  )
}
