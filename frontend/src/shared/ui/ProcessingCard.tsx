import { Sparkles } from 'lucide-react'

/** Свойства карточки долгой операции. */
interface ProcessingCardProps {
  title: string
  subtitle: string
  count?: number
}

/** Карточка долгой операции на backend (генерация) со счётчиком уже созданного. */
export function ProcessingCard({ title, subtitle, count }: ProcessingCardProps) {
  return (
    <div className="processing-card">
      <div className="processing-orb">
        <Sparkles size={21} />
      </div>
      <div>
        <h3>
          {title}
          <span className="ellipsis">…</span>
        </h3>
        <p className="muted">{subtitle}</p>
      </div>
      {typeof count === 'number' && <strong className="processing-count">{count}</strong>}
      <div className="processing-line" />
    </div>
  )
}
