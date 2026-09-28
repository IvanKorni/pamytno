import { formatPercent } from '@/shared'
import type { TopicProgress } from '../model/types'

/** Сколько делений в текстовой полосе прогресса. */
const BLOCKS = 20

/** Прогресс темы: крупный процент, показатели карточек и распределение по состояниям. */
export function ProgressOverview({ progress }: { progress: TopicProgress }) {
  const rows: [string, number][] = [
    ['Всего карточек', progress.totalCards],
    ['Изучено', progress.masteredCards],
    ['На изучении', progress.learningCards],
    ['Новых', progress.newCards],
    ['Повторить сейчас', progress.dueCards],
    ['До конца сегодня', progress.dueToday],
  ]
  return (
    <div className="progress-view">
      <div className="progress-summary">
        <div className="progress-big">{formatPercent(progress.progress)}</div>
        <div className="progress-caption">
          <span className="label-caps">Изучено</span>
          <span className="progress-blocks" aria-hidden="true">
            {blocks(progress.progress)}
          </span>
        </div>
      </div>
      <div className="progress-columns">
        <ProgressTable title="Карточки" rows={rows} />
        <Distribution progress={progress} />
      </div>
    </div>
  )
}

/** Таблица показателей: подпись и значение в строке. */
function ProgressTable({ title, rows }: { title: string; rows: [string, number][] }) {
  return (
    <div>
      <div className="label-caps">{title}</div>
      <div className="progress-rows">
        {rows.map(([label, value]) => (
          <div key={label} className="progress-row">
            <span>{label}</span>
            <span className="progress-value">{value}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

/** Доли карточек по состояниям полосами; изученной считается карточка, прошедшая все этапы. */
function Distribution({ progress }: { progress: TopicProgress }) {
  const parts: [string, number][] = [
    ['Изучено', progress.masteredCards],
    ['На изучении', progress.learningCards],
    ['Новые', progress.newCards],
  ]
  const share = (value: number) => (progress.totalCards ? (value / progress.totalCards) * 100 : 0)
  return (
    <div>
      <div className="label-caps">Распределение</div>
      <div className="progress-rows">
        {parts.map(([label, value]) => (
          <div key={label} className="progress-row">
            <span className="progress-label">{label}</span>
            <span className="progress-track">
              <span style={{ width: `${share(value)}%` }} />
            </span>
            <span className="progress-value">{value}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

/** Текстовая полоса прогресса из закрашенных и пустых делений. */
function blocks(percent: number): string {
  const filled = Math.round((Math.min(Math.max(percent, 0), 100) / 100) * BLOCKS)
  return '█'.repeat(filled) + '░'.repeat(BLOCKS - filled)
}
