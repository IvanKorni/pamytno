import { describe, expect, it } from 'vitest'
import { formatPercent, plural } from './format'

describe('утилиты представления', () => {
  it('форматирует процент с одной значащей дробью', () => {
    expect(formatPercent(68.24)).toBe('68,2%')
    expect(formatPercent(0)).toBe('0%')
  })

  it('выбирает правильную форму слова', () => {
    expect(plural(1, 'карточка', 'карточки', 'карточек')).toBe('карточка')
    expect(plural(22, 'карточка', 'карточки', 'карточек')).toBe('карточки')
    expect(plural(15, 'карточка', 'карточки', 'карточек')).toBe('карточек')
  })
})
