export type Id = string

export type TopicStatus = 'DRAFT' | 'PROCESSING' | 'READY' | 'ERROR'
export type SourceType = 'PDF' | 'YOUTUBE' | 'TEXT' | 'WORD_LIST'
export type SourceStatus = 'UPLOADED' | 'PROCESSING' | 'READY' | 'ERROR'
export type QuestionStatus = 'GENERATED' | 'APPROVED' | 'REJECTED' | 'CARD_CREATED'
export type GenerationJobStatus = 'PROCESSING' | 'READY' | 'ERROR'
export type ReviewResult = 'REMEMBER' | 'FORGOT' | 'CONTINUE'

export interface User { id: Id; email: string; createdAt: string }
export interface AuthToken { accessToken: string; tokenType: string; expiresIn: number; userId: Id }
export interface Topic { id: Id; title: string; description?: string; status: TopicStatus; createdAt: string; updatedAt: string }
export interface Source {
  id: Id; topicId: Id; type: SourceType; status: SourceStatus; originalName?: string; originalUrl?: string
  errorCode?: string; errorMessage?: string; extractedTextLength?: number; createdAt: string; updatedAt: string
}
export interface SourceText { sourceId: Id; originalText?: string; extractedText?: string }
export interface TopicContent { topicId: Id; version: number; content: string; createdAt: string }
export interface GenerationJob {
  id: Id; topicId: Id; type: 'QUESTIONS' | 'CARDS'; status: GenerationJobStatus; itemsCreated: number
  errorMessage?: string; createdAt: string; completedAt?: string
}
export interface Question { id: Id; topicId: Id; chunkId?: Id; text: string; sourceFragment?: string; status: QuestionStatus; createdAt: string }
export interface Flashcard {
  id: Id; topicId: Id; questionId?: Id; front: string; back: string; sourceFragment?: string; createdAt: string; updatedAt: string
}
export interface DueCard { cardId: Id; topicId: Id; front: string; back: string; stage: number; nextReviewAt?: string; totalReviews: number }
export interface ReviewOutcome {
  cardId: Id; result: ReviewResult; stage: number; nextReviewAt?: string; mastered: boolean; returnToSession: boolean
}
export interface LearningSession {
  id: Id; topicId: Id; startedAt: string; completedAt?: string; cardsTotal: number; cardsRemembered: number; cardsForgotten: number
}
export interface TopicProgress {
  topicId: Id; totalCards: number; newCards: number; learningCards: number; masteredCards: number; dueCards: number; dueToday: number; progress: number
}
export interface Dashboard extends TopicProgress { topics: TopicProgress[] }
export interface ApiError { code?: string; message?: string; timestamp?: string }

