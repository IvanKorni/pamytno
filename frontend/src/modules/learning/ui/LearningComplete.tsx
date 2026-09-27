import type { ReactNode } from 'react'
import { Stat } from '@/shared'

/** Свойства экрана завершения сессии. */
interface LearningCompleteProps {
  remembered: number
  forgotten: number
  children: ReactNode
}

/** Итоги учебной сессии; куда идти дальше, решает страница через `children`. */
export function LearningComplete({ remembered, forgotten, children }: LearningCompleteProps) {
  return (
    <div className="complete-page">
      <div className="complete-mark">✓</div>
      <div className="eyebrow">Сессия завершена</div>
      <h1>Готово 🎉</h1>
      <p className="muted">Хорошая работа. Знания становятся крепче с каждым повторением.</p>
      <div className="complete-stats">
        <Stat label="Ответов" value={remembered + forgotten} />
        <Stat label="Помню" value={remembered} />
        <Stat label="Не помню" value={forgotten} />
      </div>
      <div className="complete-actions">{children}</div>
    </div>
  )
}
