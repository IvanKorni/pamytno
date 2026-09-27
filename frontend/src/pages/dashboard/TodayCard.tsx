import { ChevronRight, Flame } from 'lucide-react'
import { plural } from '@/shared'

/** Свойства блока «Сегодня». */
interface TodayCardProps {
  dueNow: number
  dueToday: number
  onStart: () => void
}

/**
 * Блок «Сегодня»: сколько карточек можно повторить прямо сейчас и сколько ещё подойдёт до конца дня.
 * Кнопка «Начать» есть, только когда повторять есть что уже сейчас.
 */
export function TodayCard({ dueNow, dueToday, onStart }: TodayCardProps) {
  return (
    <section className="today-card">
      <div className="today-icon">
        <Flame size={24} />
      </div>
      <div>
        <span className="eyebrow">Сегодня</span>
        <h2>
          {dueNow} {plural(dueNow, 'карточка', 'карточки', 'карточек')} к повторению
        </h2>
        <p className="muted">{todayHint(dueNow, Math.max(dueToday - dueNow, 0))}</p>
      </div>
      {dueNow > 0 && (
        <button className="button button-dark" onClick={onStart}>
          Начать <ChevronRight size={17} />
        </button>
      )}
    </section>
  )
}

/** Подсказка под счётчиком: повторять сейчас, позже сегодня или всё чисто. */
function todayHint(dueNow: number, dueLater: number): string {
  if (dueNow) return 'Самое время освежить то, что уже начинали.'
  if (dueLater) return `Ещё ${dueLater} ${plural(dueLater, 'карточка подойдёт', 'карточки подойдут', 'карточек подойдут')} до конца дня.`
  return 'Отлично — на сегодня всё чисто.'
}
