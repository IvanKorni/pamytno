import { describe, expect, it } from 'vitest'
import { EMPTY_DRAFT, isDraftReady } from './materialDraft'

describe('черновик материала', () => {
  it('готов к отправке, когда заполнено главное поле выбранного типа', () => {
    expect(isDraftReady({ ...EMPTY_DRAFT, text: 'MVCC' })).toBe(true)
    expect(isDraftReady({ ...EMPTY_DRAFT, kind: 'YOUTUBE', url: 'https://youtu.be/abc' })).toBe(true)
    expect(isDraftReady({ ...EMPTY_DRAFT, kind: 'PDF', file: new File(['%PDF'], 'a.pdf') })).toBe(true)
  })

  it('не готов, если главное поле пустое или из одних пробелов', () => {
    expect(isDraftReady({ ...EMPTY_DRAFT, text: '   ' })).toBe(false)
    expect(isDraftReady({ ...EMPTY_DRAFT, kind: 'YOUTUBE', url: '  ' })).toBe(false)
    expect(isDraftReady({ ...EMPTY_DRAFT, kind: 'PDF' })).toBe(false)
  })
})
