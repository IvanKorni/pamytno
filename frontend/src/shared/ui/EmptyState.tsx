import type { ReactNode } from 'react'

/** Свойства пустого состояния. */
interface EmptyStateProps {
  title: string
  text: string
  action?: ReactNode
}

/** Пустое состояние экрана: крупная фраза, пояснение и действие, которое поможет начать. */
export function EmptyState({ title, text, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <h2>{title}</h2>
      <p>{text}</p>
      {action}
    </div>
  )
}
