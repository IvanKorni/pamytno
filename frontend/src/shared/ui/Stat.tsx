/** Свойства показателя. */
interface StatProps {
  label: string
  value: string | number
  accent?: boolean
}

/** Показатель в строке статистики: подпись и значение. */
export function Stat({ label, value, accent = false }: StatProps) {
  return (
    <div className="stat">
      <span className="muted small">{label}</span>
      <strong className={accent ? 'accent-text' : ''}>{value}</strong>
    </div>
  )
}

/** Показатель в отдельной карточке. */
export function StatCard({ label, value, accent = false }: StatProps) {
  return (
    <div className="stat-card">
      <span className="muted small">{label}</span>
      <strong className={accent ? 'accent-text' : ''}>{value}</strong>
    </div>
  )
}
