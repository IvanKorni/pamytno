import { describe, expect, it } from 'vitest'
import { EMPTY_WORDS_DRAFT, isWordsDraftReady, toVocabularyRequest } from './wordsDraft'

describe('Черновик карточек слов', () => {
  it('для всех слов не передаёт инструкцию, для жирных и коллокаций — готовую', () => {
    // given
    const draft = { ...EMPTY_WORDS_DRAFT, text: 'contract — договор' }

    // when
    const all = toVocabularyRequest(draft)
    const bold = toVocabularyRequest({ ...draft, mode: 'BOLD' })
    const collocations = toVocabularyRequest({ ...draft, mode: 'COLLOCATIONS' })

    // then
    expect(all).toEqual({ text: 'contract — договор' })
    expect(bold.instruction).toContain('жирным')
    expect(collocations.instruction).toContain('коллокации')
  })

  it('своя инструкция обязательна и передаётся без лишних пробелов', () => {
    // given
    const draft = { mode: 'CUSTOM' as const, text: 'Some text', customInstruction: '  ' }

    // when
    const withoutInstruction = isWordsDraftReady(draft)
    const request = toVocabularyRequest({ ...draft, customInstruction: ' фразовые глаголы ' })

    // then
    expect(withoutInstruction).toBe(false)
    expect(request).toEqual({ text: 'Some text', instruction: 'фразовые глаголы' })
  })

  it('без текста отправить нельзя', () => {
    // when
    const ready = isWordsDraftReady({ ...EMPTY_WORDS_DRAFT, text: ' \n ' })

    // then
    expect(ready).toBe(false)
  })
})
