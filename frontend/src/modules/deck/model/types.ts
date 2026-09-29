import type { components } from '../api/schema.gen'

/** Схемы контракта deck-api. */
type Schemas = components['schemas']

/** Сгенерированный вопрос. */
export type Question = Schemas['QuestionDto']

/** Статус вопроса. */
export type QuestionStatus = Schemas['QuestionStatus']

/** Решение пользователя по вопросу. */
export type QuestionDecision = Schemas['QuestionDecision']

/** Асинхронная задача генерации вопросов, карточек или карточек слов. */
export type GenerationJob = Schemas['GenerationJobDto']

/** Карточка. */
export type Flashcard = Schemas['FlashcardDto']

/** Текст и инструкция для карточек английских слов. */
export type VocabularyRequest = Schemas['GenerateVocabularyRequest']

/** Изменяемые поля карточки. */
export type CardChanges = Schemas['UpdateCardRequest']
