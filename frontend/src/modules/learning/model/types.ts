import type { components } from '../api/schema.gen'

/** Схемы контракта learning-api. */
type Schemas = components['schemas']

/** Карточка к повторению. */
export type DueCard = Schemas['DueCardDto']

/** Ответ пользователя по карточке. */
export type ReviewResult = Schemas['ReviewResult']

/** Новое состояние карточки после ответа. */
export type ReviewOutcome = Schemas['ReviewResultDto']

/** Учебная сессия. */
export type LearningSession = Schemas['LearningSessionDto']

/** Прогресс изучения темы. */
export type TopicProgress = Schemas['TopicProgressDto']

/** Общий прогресс по всем темам. */
export type Dashboard = Schemas['DashboardDto']
