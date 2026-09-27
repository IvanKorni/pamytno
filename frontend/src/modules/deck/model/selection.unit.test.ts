import { describe, expect, it } from 'vitest'
import { pendingSelection } from './selection'
import type { Question } from './types'

const question = (id: string, status: Question['status']): Question => ({
  id,
  topicId: 't-1',
  text: id,
  status,
  createdAt: '2026-09-27T10:00:00Z',
})

describe('выбор вопросов', () => {
  it('оставляет в выборе только новые вопросы', () => {
    // given
    const questions = [question('a', 'GENERATED'), question('b', 'REJECTED'), question('c', 'CARD_CREATED')]

    // when
    const selected = pendingSelection(['a', 'b', 'c', 'gone'], questions)

    // then
    expect(selected).toEqual(['a'])
  })
})
