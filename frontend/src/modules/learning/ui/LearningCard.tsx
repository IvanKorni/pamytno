import { InlineError, isShortText, RichInline, RichText, useKeyboardShortcuts } from '@/shared'
import type { DueCard } from '../model/types'

/** Число этапов повторения: на этапах 0–5 карточка учится, после шестого считается изученной. */
const STAGES = 6

/** Свойства карточки в режиме обучения. */
interface LearningCardProps {
  card: DueCard
  revealed: boolean
  reveal: () => void
  answer: (result: 'REMEMBER' | 'FORGOT') => void
  answering: boolean
  error?: string
}

/**
 * Карточка в режиме обучения: вопрос, ответ по запросу и оценка «Помню» / «Не помню».
 * Пробел открывает ответ, стрелки влево и вправо — «Не помню» и «Помню».
 */
export function LearningCard({ card, revealed, reveal, answer, answering, error }: LearningCardProps) {
  const rate = (result: 'REMEMBER' | 'FORGOT') => revealed && !answering && answer(result)
  useKeyboardShortcuts({
    ' ': () => !revealed && reveal(),
    ArrowLeft: () => rate('FORGOT'),
    ArrowRight: () => rate('REMEMBER'),
  })
  return (
    <div className="learning-card">
      <div className="learning-body">
        <div className="mono">{cardHint(card)}</div>
        <Question text={card.front} />
        {revealed && <RichText text={card.back} className="learning-answer" />}
      </div>
      <div className="learning-controls">
        {error && <InlineError message={`Ответ не сохранён: ${error}`} />}
        {revealed ? <AnswerButtons answering={answering} answer={answer} /> : <RevealButton reveal={reveal} />}
      </div>
    </div>
  )
}

/** Кнопка «Показать ответ» с подсказкой про пробел. */
function RevealButton({ reveal }: { reveal: () => void }) {
  return (
    <>
      <button className="button button-primary button-wide learning-button" onClick={reveal}>
        Показать ответ
      </button>
      <div className="mono key-hint">пробел</div>
    </>
  )
}

/** Кнопки оценки ответа с подсказкой про стрелки. */
function AnswerButtons({ answering, answer }: Pick<LearningCardProps, 'answering' | 'answer'>) {
  return (
    <>
      <div className="learning-actions">
        <button
          className="button button-secondary learning-button"
          disabled={answering}
          onClick={() => answer('FORGOT')}
        >
          Не помню
        </button>
        <button
          className="button button-primary learning-button"
          disabled={answering}
          onClick={() => answer('REMEMBER')}
        >
          Помню
        </button>
      </div>
      <div className="mono key-hint">← не помню · помню →</div>
    </>
  )
}

/** Вопрос карточки: короткий — крупным заголовком, длинный (задача с вариантами) — обычным текстом для чтения. */
function Question({ text }: { text: string }) {
  if (isShortText(text)) {
    return (
      <h1 className="learning-front">
        <RichInline text={text} />
      </h1>
    )
  }
  return <RichText text={text} className="learning-front is-long" />
}

/** Подпись над вопросом: новая карточка или этап повторения и сколько раз её уже повторяли. */
function cardHint(card: DueCard): string {
  if (!card.totalReviews) return 'новая карточка'
  return `этап ${Math.min(card.stage + 1, STAGES)} из ${STAGES} · повторений: ${card.totalReviews}`
}
