import { Link } from 'react-router-dom'
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
      <button className="button button-primary" disabled={!toStudy || cardRun.running} onClick={() => cardRun.launch()}>
        Создать карточки · {toStudy}
      </button>
      <span className="mono">
        {toStudy} {plural(toStudy, 'вопрос', 'вопроса', 'вопросов')} к изучению
      </span>
      {selected.length > 0 && (
        <div className="footer-links">
          <button className="link-button" onClick={() => workflow.setSelected([])}>
            Снять выбор
          </button>
          <button className="link-button" disabled={deciding} onClick={workflow.rejectSelected}>
            Не изучать выбранные
          </button>
        </div>
      )}
    </div>
  )
}

/** Свойства итога генерации карточек. */
interface CardsReadyProps {
  topicId: string
  count: number
  onBack: () => void
}

/** Итог генерации карточек крупным текстом: к обучению, к карточкам или обратно к оставшимся вопросам. */
export function CardsReady({ topicId, count, onBack }: CardsReadyProps) {
  return (
    <div className="processing">
      <h2 className="processing-title">
        {count} {plural(count, 'карточка готова', 'карточки готовы', 'карточек готовы')}
      </h2>
      <p className="muted">Первое повторение — сразу. Дальше интервалы подберутся сами.</p>
      <div className="button-row cards-ready-actions">
        <Link className="button button-primary" to={`/topics/${topicId}/learn`}>
          Начать обучение
        </Link>
        <Link className="button button-secondary" to={`/topics/${topicId}/cards`}>
          Посмотреть карточки
        </Link>
        <button className="link-button is-muted" onClick={onBack}>
          К вопросам
        </button>
      </div>
    </div>
  )
}
