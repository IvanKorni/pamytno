import type { QuestionStatus } from './types'

/** Подписи статусов вопроса. */
const QUESTION_STATUS_LABELS: Record<QuestionStatus, string> = {
  GENERATED: 'Новый',
  APPROVED: 'Выбран',
  REJECTED: 'Пропущен',
  CARD_CREATED: 'В карточках',
}

/** Подпись статуса вопроса для пользователя. */
export const questionStatusLabel = (status: QuestionStatus) => QUESTION_STATUS_LABELS[status]
