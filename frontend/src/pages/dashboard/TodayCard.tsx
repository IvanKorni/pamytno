import { ChevronRight, Flame } from 'lucide-react'
import { plural } from '@/shared'

/** Свойства блока «Сегодня». */
interface TodayCardProps {
  dueCount: number
  onStart: () => void
}

/** Блок «Сегодня»: сколько карточек ждут повторения и кнопка начать. */
export function TodayCard({ dueCount, onStart }: TodayCardProps) {
  return (
    <section className="today-card">
      <div className="today-icon">
        <Flame size={24} />
      </div>
      <div>
        <span className="eyebrow">Сегодня</span>
        <h2>
          {dueCount} {plural(dueCount, 'карточка', 'карточки', 'карточек')} к повторению
        </h2>
        <p className="muted">
          {dueCount ? 'Самое время освежить то, что уже начинали.' : 'Отлично — на сегодня всё чисто.'}
        </p>
      </div>
      {dueCount > 0 && (
        <button className="button button-dark" onClick={onStart}>
          Начать <ChevronRight size={17} />
        </button>
      )}
    </section>
  )
}
