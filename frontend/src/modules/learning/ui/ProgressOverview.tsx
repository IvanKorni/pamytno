import type { CSSProperties } from 'react'
import { formatPercent } from '@/shared'
import type { TopicProgress } from '../model/types'

/** Общий прогресс темы: кольцо и распределение карточек по состояниям. */
export function ProgressOverview({ progress }: { progress: TopicProgress }) {
  const ring = { '--progress': `${progress.progress}%` } as CSSProperties
  return (
    <div className="progress-overview">
      <div className="big-progress">
        <div className="ring" style={ring}>
          <strong>{formatPercent(progress.progress)}</strong>
        </div>
        <div>
          <div className="eyebrow">Общий прогресс</div>
          <h3>
            {progress.masteredCards} из {progress.totalCards} карточек изучено
          </h3>
          <p className="muted">Изученной считается карточка, которая прошла все этапы повторения.</p>
        </div>
      </div>
      <div className="progress-bars">
        <ProgressLine label="Изучено" value={progress.masteredCards} total={progress.totalCards} color="green" />
        <ProgressLine label="На изучении" value={progress.learningCards} total={progress.totalCards} color="orange" />
        <ProgressLine label="Новые" value={progress.newCards} total={progress.totalCards} color="gray" />
      </div>
    </div>
  )
}

/** Свойства полосы прогресса. */
interface ProgressLineProps {
  label: string
  value: number
  total: number
  color: string
}

/** Полоса с долей карточек в одном состоянии. */
function ProgressLine({ label, value, total, color }: ProgressLineProps) {
  return (
    <div className="progress-line">
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
      <div className="progress-track">
        <span className={`bar-${color}`} style={{ width: `${total ? (value / total) * 100 : 0}%` }} />
      </div>
    </div>
  )
}
