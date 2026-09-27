import type { components } from '../api/schema.gen'

/** Схемы контракта deck-api. */
type Schemas = components['schemas']

/** Сгенерированный вопрос. */
export type Question = Schemas['QuestionDto']

/** Статус вопроса. */
export type QuestionStatus = Schemas['QuestionStatus']

/** Решение пользователя по вопросу. */
export type QuestionDecision = Schemas['QuestionDecision']

/** Асинхронная задача генерации вопросов или карточек. */
export type GenerationJob = Schemas['GenerationJobDto']

/** Карточка. */
export type Flashcard = Schemas['FlashcardDto']

/** Изменяемые поля карточки. */
export type CardChanges = Schemas['UpdateCardRequest']
