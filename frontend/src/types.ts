import type { components as DeckContract } from './modules/deck/api/schema.gen'
import type { components as IdentityContract } from './modules/identity/api/schema.gen'
import type { components as LearningContract } from './modules/learning/api/schema.gen'
import type { components as TopicContract } from './modules/topic/api/schema.gen'

type Identity = IdentityContract['schemas']
type TopicSchemas = TopicContract['schemas']
type Deck = DeckContract['schemas']
type Learning = LearningContract['schemas']

export type TopicStatus = TopicSchemas['TopicStatus']
export type SourceType = TopicSchemas['SourceType']
export type SourceStatus = TopicSchemas['SourceStatus']
export type QuestionStatus = Deck['QuestionStatus']
export type GenerationJobStatus = Deck['GenerationJobStatus']
export type ReviewResult = Learning['ReviewResult']

export type User = Identity['UserDto']
export type AuthToken = Identity['AuthTokenDto']
export type Topic = TopicSchemas['TopicDto']
export type Source = TopicSchemas['SourceDto']
export type SourceText = TopicSchemas['SourceTextDto']
export type TopicContent = TopicSchemas['TopicContentDto']
export type GenerationJob = Deck['GenerationJobDto']
export type Question = Deck['QuestionDto']
export type Flashcard = Deck['FlashcardDto']
export type DueCard = Learning['DueCardDto']
export type ReviewOutcome = Learning['ReviewResultDto']
export type LearningSession = Learning['LearningSessionDto']
export type TopicProgress = Learning['TopicProgressDto']
export type Dashboard = Learning['DashboardDto']
export type ApiError = Partial<Identity['ErrorResponse']>
