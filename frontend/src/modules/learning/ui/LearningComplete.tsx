import type { ReactNode } from 'react'

/** Свойства экрана завершения сессии. */
interface LearningCompleteProps {
  title?: string
  remembered: number
  forgotten: number
  children: ReactNode
}

/** Итоги учебной сессии крупной типографикой; куда идти дальше, решает страница через `children`. */
export function LearningComplete({ title, remembered, forgotten, children }: LearningCompleteProps) {
  return (
    <section className="complete">
      <div className="label-caps">
        <span>Сессия завершена</span>
        {title && <span> · {title}</span>}
      </div>
      <h1 className="complete-title">Готово.</h1>
      <p className="complete-lead">Ответов за сессию: {remembered + forgotten}</p>
      <div className="complete-stats">
        <CompleteStat label="Помню" value={remembered} main />
        <CompleteStat label="Не помню" value={forgotten} />
      </div>
      <div className="button-row complete-actions">{children}</div>
    </section>
  )
}

/** Одно число итогов с подписью; главное отмечено тёмной линией сверху. */
function CompleteStat({ label, value, main = false }: { label: string; value: number; main?: boolean }) {
  return (
    <div className={main ? 'complete-stat is-main' : 'complete-stat'}>
      <div>{label}</div>
      <div className="complete-value">{value}</div>
    </div>
  )
}
