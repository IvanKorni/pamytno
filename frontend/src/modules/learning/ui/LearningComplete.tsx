import { Link } from 'react-router-dom'
import { Stat } from '@/shared'

/** Свойства экрана завершения сессии. */
interface LearningCompleteProps {
  session?: { cardsRemembered: number; cardsForgotten: number }
  remembered: number
  forgotten: number
  reset: () => void
}

/** Экран завершения учебной сессии с её итогами. */
export function LearningComplete({ session, remembered, forgotten, reset }: LearningCompleteProps) {
  const total = (session?.cardsRemembered || 0) + (session?.cardsForgotten || 0) || remembered + forgotten
  return (
    <div className="complete-page">
      <div className="complete-mark">✓</div>
      <div className="eyebrow">Сессия завершена</div>
      <h1>Готово 🎉</h1>
      <p className="muted">Хорошая работа. Знания становятся крепче с каждым повторением.</p>
      <div className="complete-stats">
        <Stat label="Пройдено" value={total} />
        <Stat label="Помню" value={session?.cardsRemembered || remembered} />
        <Stat label="Не помню" value={session?.cardsForgotten || forgotten} />
      </div>
      <div className="complete-actions">
        <Link className="button button-primary" to=".." onClick={reset}>
          На главную темы
        </Link>
        <button className="button button-secondary" onClick={reset}>
          Закрыть
        </button>
      </div>
    </div>
  )
}
