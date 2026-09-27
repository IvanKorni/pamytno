import { Link } from 'react-router-dom'
import { Check, ChevronRight, Play } from 'lucide-react'
import type { QuestionsWorkflow } from '@/modules/deck'
import { plural } from '@/shared'

/**
 * Панель действий под вопросами: сколько вопросов пойдёт в карточки, снятие выбора,
 * отказ от выбранных и создание карточек.
 */
export function QuestionsFooter({ workflow }: { workflow: QuestionsWorkflow }) {
  const { selected, cardRun, deciding } = workflow
  const approved = (workflow.questions.data ?? []).filter((item) => item.status === 'APPROVED').length
  const toStudy = approved + selected.length
  return (
    <div className="question-footer">
      <div>
        <strong>{toStudy}</strong> {plural(toStudy, 'вопрос', 'вопроса', 'вопросов')} к изучению
      </div>
      <div className="footer-actions">
        {selected.length > 0 && (
          <>
            <button className="button button-secondary" onClick={() => workflow.setSelected([])}>
              Снять выбор
            </button>
            <button className="button button-secondary" disabled={deciding} onClick={workflow.rejectSelected}>
              Не изучать выбранные
            </button>
          </>
        )}
        <button className="button button-primary" disabled={!toStudy || cardRun.running} onClick={cardRun.launch}>
          {cardRun.running ? 'Создаём карточки…' : 'Создать карточки'} <ChevronRight size={16} />
        </button>
      </div>
    </div>
  )
}

/** Баннер после генерации карточек с переходом к обучению. */
export function CardsReadyBanner({ topicId, count }: { topicId: string; count: number }) {
  return (
    <div className="success-banner">
      <div className="success-icon">
        <Check size={18} />
      </div>
      <div>
        <strong>
          {count} {plural(count, 'карточка создана', 'карточки созданы', 'карточек создано')}
        </strong>
        <p className="muted">Можно начать обучение прямо сейчас.</p>
      </div>
      <Link className="button button-dark" to={`/topics/${topicId}/learn`}>
        Начать обучение <Play size={15} fill="currentColor" />
      </Link>
    </div>
  )
}
