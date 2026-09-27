/** Время, которым помечены тестовые данные. */
const AT = '2026-09-27T10:00:00Z'

/** Тема в тестах экранов. */
export function topic(id = 'topic-1', title = 'Транзакции PostgreSQL') {
  return { id, title, description: 'Изоляция и блокировки', status: 'READY', createdAt: AT, updatedAt: AT }
}

/** Прогресс темы в тестах экранов. */
export function progress(topicId = 'topic-1', overrides: Record<string, number> = {}) {
  return {
    topicId,
    totalCards: 2,
    newCards: 2,
    learningCards: 0,
    masteredCards: 0,
    dueCards: 2,
    dueToday: 2,
    progress: 0,
    ...overrides,
  }
}

/** Карточка к повторению в тестах экранов. */
export function dueCard(cardId: string, front: string, topicId = 'topic-1') {
  return { cardId, topicId, front, back: `Ответ: ${front}`, stage: 0, totalReviews: 0 }
}

/** Вопрос в тестах экранов. */
export function question(id: string, text: string, status = 'GENERATED', topicId = 'topic-1') {
  return { id, topicId, text, status, createdAt: AT }
}

/** Задача генерации в тестах экранов. */
export function job(id: string, status: string, overrides: Record<string, unknown> = {}) {
  return { id, topicId: 'topic-1', type: 'QUESTIONS', status, itemsCreated: 0, createdAt: AT, ...overrides }
}

/** Учебная сессия в тестах экранов. */
export function session(id = 'session-1', topicId = 'topic-1') {
  return { id, topicId, startedAt: AT, cardsTotal: 2, cardsRemembered: 0, cardsForgotten: 0 }
}
