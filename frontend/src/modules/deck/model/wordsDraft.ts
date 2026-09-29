import type { VocabularyRequest } from './types'

/** Что взять из вставленного текста: все слова, выделенные жирным, коллокации или своя инструкция. */
export type WordsMode = 'ALL' | 'BOLD' | 'COLLOCATIONS' | 'CUSTOM'

/** Режим окна карточек слов: подпись вкладки, инструкция для AI и подсказки к полю текста. */
export interface WordsModeInfo {
  mode: WordsMode
  label: string
  instruction: string
  hint: string
  placeholder: string
}

/** Режимы в порядке вкладок; подписи короткие, чтобы вкладки помещались на телефоне. Своя инструкция — из поля. */
export const WORDS_MODES: WordsModeInfo[] = [
  {
    mode: 'ALL',
    label: 'Все',
    instruction: '',
    hint: 'По слову или выражению на строку, можно с переводом.',
    placeholder: 'contract — договор\nmake a decision\nreliable',
  },
  {
    mode: 'BOLD',
    label: 'Жирные',
    instruction: 'слова и выражения, выделенные жирным',
    hint: 'Вставьте текст из учебника или с сайта — жирное выделение сохранится как **слово**.',
    placeholder: 'I had to **make a decision** quickly…',
  },
  {
    mode: 'COLLOCATIONS',
    label: 'Коллокации',
    instruction: 'коллокации — устойчивые сочетания слов',
    hint: 'Вставьте текст — AI найдёт в нём устойчивые сочетания.',
    placeholder: 'Governments need to take action and reduce carbon emissions…',
  },
  {
    mode: 'CUSTOM',
    label: 'Своя',
    instruction: '',
    hint: 'Вставьте слова или текст.',
    placeholder: 'Вставьте слова или текст…',
  },
]

/** Максимальная длина текста — как в контракте backend. */
export const MAX_WORDS_TEXT = 20_000

/** Черновик окна карточек слов. */
export interface WordsDraft {
  mode: WordsMode
  text: string
  customInstruction: string
}

/** Пустой черновик: все слова из списка. */
export const EMPTY_WORDS_DRAFT: WordsDraft = { mode: 'ALL', text: '', customInstruction: '' }

/** Описание режима черновика. */
export function modeInfo(mode: WordsMode): WordsModeInfo {
  return WORDS_MODES.find((info) => info.mode === mode) ?? WORDS_MODES[0]
}

/** Черновик готов к отправке: есть текст, а для своей инструкции — и сама инструкция. */
export function isWordsDraftReady(draft: WordsDraft): boolean {
  if (!draft.text.trim()) return false
  return draft.mode !== 'CUSTOM' || Boolean(draft.customInstruction.trim())
}

/** Запрос к backend: текст как есть, инструкция по режиму; пустая инструкция не передаётся. */
export function toVocabularyRequest(draft: WordsDraft): VocabularyRequest {
  const instruction = draft.mode === 'CUSTOM' ? draft.customInstruction.trim() : modeInfo(draft.mode).instruction
  return instruction ? { text: draft.text, instruction } : { text: draft.text }
}
