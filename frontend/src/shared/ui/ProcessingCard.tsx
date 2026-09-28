/** Свойства блока долгой операции. */
interface ProcessingCardProps {
  title: string
  subtitle: string
  count?: number
}

/**
 * Долгая операция на backend (генерация) крупным текстом: что происходит, пояснение и сколько уже готово.
 * Счётчик появляется, только когда backend что-то уже создал.
 */
export function ProcessingCard({ title, subtitle, count }: ProcessingCardProps) {
  return (
    <div className="processing" role="status">
      <div className="processing-title">
        {title}
        <span className="ellipsis">…</span>
      </div>
      {Boolean(count) && <div className="processing-count">готово: {count}</div>}
      <div className="processing-line" />
      <span className="mono">{subtitle}</span>
    </div>
  )
}
