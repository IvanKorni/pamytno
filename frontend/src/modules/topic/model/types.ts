import type { components } from '../api/schema.gen'

/** Схемы контракта topic-api. */
type Schemas = components['schemas']

/** Тема — набор материалов для изучения. */
export type Topic = Schemas['TopicDto']

/** Статус темы. */
export type TopicStatus = Schemas['TopicStatus']

/** Источник материала внутри темы. */
export type Source = Schemas['SourceDto']

/** Тип источника: PDF, YouTube, текст или список слов. */
export type SourceType = Schemas['SourceType']

/** Тип текстового источника. */
export type TextSourceType = Schemas['TextSourceType']

/** Единый текст темы. */
export type TopicContent = Schemas['TopicContentDto']
