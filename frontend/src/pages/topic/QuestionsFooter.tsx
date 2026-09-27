import { useNavigate } from 'react-router-dom'
import { Check, ChevronRight, Play } from 'lucide-react'
import type { useQuestionsWorkflow } from '@/modules/deck'
import { plural } from '@/shared'

/** Свойства панели действий под вопросами. */
interface QuestionsFooterProps {
  workflow: ReturnType<typeof useQuestionsWorkflow>
  approved: number
}

/** Панель действий: сколько вопросов выбрано и создание карточек. */
export function QuestionsFooter({ workflow, approved }: QuestionsFooterProps) {
  const { selected, bulk, makeCards, processingCards } = workflow
  return (
    <div className="question-footer">
      <div>
        <strong>{approved}</strong> {plural(approved, 'вопрос выбран', 'вопроса выбрано', 'вопросов выбрано')}
      </div>
      <div className="footer-actions">
        <button
          className="button button-secondary"
          disabled={!selected.length || bulk.isPending}
          onClick={() => bulk.mutate('REJECT')}
        >
          Снять выбор
        </button>
        <button
          className="button button-primary"
          disabled={(!approved && !selected.length) || processingCards}
          onClick={() => makeCards.mutate()}
        >
          {processingCards ? (
            'Создаём карточки…'
          ) : (
            <>
              Создать карточки <ChevronRight size={16} />
            </>
          )}
        </button>
      </div>
    </div>
  )
}

/** Баннер с готовыми карточками и переходом к обучению. */
export function CardsReadyBanner({ count }: { count: number }) {
  const navigate = useNavigate()
  return (
    <div className="success-banner">
      <div className="success-icon">
        <Check size={18} />
      </div>
      <div>
        <strong>
          {count} {plural(count, 'карточка готова', 'карточки готовы', 'карточек готовы')}
        </strong>
        <p className="muted">Можно начать обучение прямо сейчас.</p>
      </div>
      <button className="button button-dark" onClick={() => navigate('../learn')}>
        Начать обучение <Play size={15} fill="currentColor" />
      </button>
    </div>
  )
}
