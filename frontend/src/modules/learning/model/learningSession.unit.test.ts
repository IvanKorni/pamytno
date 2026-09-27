import { describe, expect, it } from 'vitest'
import { INITIAL_LEARNING_STATE, learningReducer, type LearningState } from './learningSession'
import type { DueCard, LearningSession, ReviewOutcome } from './types'

const session: LearningSession = {
  id: 's-1',
  topicId: 't-1',
  startedAt: '2026-09-27T10:00:00Z',
  cardsTotal: 2,
  cardsRemembered: 0,
  cardsForgotten: 0,
}
const card = (cardId: string): DueCard => ({ cardId, topicId: 't-1', front: cardId, back: '', stage: 0, totalReviews: 0 })
const outcome = (cardId: string, result: 'REMEMBER' | 'FORGOT', returnToSession = false): ReviewOutcome => ({
  cardId,
  result,
  stage: 1,
  mastered: false,
  returnToSession,
})
const started = (...cards: DueCard[]): LearningState =>
  learningReducer(INITIAL_LEARNING_STATE, { type: 'started', session, cards })

describe('очередь учебной сессии', () => {
  it('начинает сессию с первой карточки и скрытым ответом', () => {
    // given / when
    const state = started(card('a'), card('b'))

    // then
    expect(state).toMatchObject({ phase: 'active', revealed: false, remembered: 0, forgotten: 0 })
    expect(state.phase === 'active' && state.queue.map((item) => item.cardId)).toEqual(['a', 'b'])
  })

  it('после «Помню» убирает карточку из очереди и снова скрывает ответ', () => {
    // given
    const state = learningReducer(started(card('a'), card('b')), { type: 'revealed' })

    // when
    const next = learningReducer(state, { type: 'answered', outcome: outcome('a', 'REMEMBER'), card: card('a') })

    // then
    expect(next).toMatchObject({ phase: 'active', revealed: false, remembered: 1, forgotten: 0 })
    expect(next.phase === 'active' && next.queue.map((item) => item.cardId)).toEqual(['b'])
  })

  it('возвращает забытую карточку в хвост очереди, если так решил backend', () => {
    // given
    const state = started(card('a'), card('b'))

    // when
    const next = learningReducer(state, { type: 'answered', outcome: outcome('a', 'FORGOT', true), card: card('a') })

    // then
    expect(next).toMatchObject({ phase: 'active', forgotten: 1 })
    expect(next.phase === 'active' && next.queue.map((item) => item.cardId)).toEqual(['b', 'a'])
  })

  it('завершает сессию после ответа на последнюю карточку', () => {
    // given
    const state = started(card('a'))

    // when
    const next = learningReducer(state, { type: 'answered', outcome: outcome('a', 'REMEMBER'), card: card('a') })

    // then
    expect(next).toMatchObject({ phase: 'finished', remembered: 1, queue: [] })
  })

  it('игнорирует повторный ответ по карточке, которой уже нет в голове очереди', () => {
    // given
    const state = learningReducer(started(card('a'), card('b')), {
      type: 'answered',
      outcome: outcome('a', 'REMEMBER'),
      card: card('a'),
    })

    // when
    const next = learningReducer(state, { type: 'answered', outcome: outcome('a', 'REMEMBER'), card: card('a') })

    // then
    expect(next.phase === 'active' && next.queue.map((item) => item.cardId)).toEqual(['b'])
  })
})
