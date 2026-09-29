import { beforeAll, describe, expect, it, vi } from 'vitest'
import { deckApi, type GenerationJob } from '@/modules/deck'
import { identityApi } from '@/modules/identity'
import { learningApi } from '@/modules/learning'
import { sourceApi, topicApi, type Source } from '@/modules/topic'
import { tokenStorage } from '@/shared'

/** Адрес настоящего backend; без него тест пропускается. */
const BACKEND = import.meta.env.VITE_PAMYTNO_INTEGRATION_URL as string | undefined

/** Сколько ждать асинхронной обработки backend (источники, генерация, проекции learning). */
const TIMEOUT_MS = 30_000

/** Материал темы: несколько предложений, чтобы AI-заглушка предложила несколько вопросов. */
const MATERIAL =
  'MVCC в PostgreSQL позволяет читателям не блокировать писателей. Каждая строка хранит xmin и xmax. ' +
  'Repeatable Read видит снимок на начало транзакции. Serializable откатывает одну из конфликтующих транзакций.'

/** Интеграционный тест запускается, только когда задан адрес backend. */
const integration = BACKEND ? describe : describe.skip

integration('путь обучения через API-слой фронта и настоящий backend (ТЗ §28)', () => {
  beforeAll(() => {
    vi.stubEnv('VITE_API_URL', `${BACKEND}/api`)
  })

  it('от регистрации до ответов по карточкам и прогресса темы', { timeout: 90_000 }, async () => {
    // given
    const token = await identityApi.register(`frontend-journey-${Date.now()}@example.com`, 'journey-password-123')
    tokenStorage.save(token.accessToken)
    const topic = await topicApi.createTopic('Транзакции PostgreSQL', 'Интеграционный путь фронта')

    // when
    await sourceApi.addTextSource(topic.id, { type: 'TEXT', name: 'Конспект', text: MATERIAL })
    await eventually(() => sourceApi.listSources(topic.id), allReady)
    const content = await sourceApi.getTopicContent(topic.id)
    await finished(await deckApi.generateQuestions(topic.id))
    const questions = await deckApi.listQuestions(topic.id)
    await deckApi.decideQuestions(
      questions.map((question) => question.id),
      'APPROVE',
    )
    await finished(await deckApi.generateCards(topic.id))
    const cards = await deckApi.listCards(topic.id)
    const due = await eventually(
      () => learningApi.getDueCards(topic.id),
      (list) => list.length === cards.length,
    )
    const session = await learningApi.startLearningSession(topic.id)
    const remembered = await learningApi.reviewCard(due[0].cardId, 'REMEMBER', session.id)
    const forgotten = await learningApi.reviewCard(due[1].cardId, 'FORGOT', session.id)
    const completed = await learningApi.completeLearningSession(session.id)
    const progress = await learningApi.getTopicProgress(topic.id)

    // then
    expect(content.content).toContain('MVCC')
    expect(questions.length).toBeGreaterThan(1)
    expect(cards).toHaveLength(questions.length)
    expect(remembered).toMatchObject({ result: 'REMEMBER', returnToSession: false })
    expect(remembered.stage).toBeGreaterThan(due[0].stage)
    expect(forgotten).toMatchObject({ result: 'FORGOT', returnToSession: true })
    expect(completed).toMatchObject({ id: session.id, cardsRemembered: 1, cardsForgotten: 1 })
    expect(completed.completedAt).toBeTruthy()
    expect(progress).toMatchObject({ totalCards: cards.length, newCards: cards.length - 2, learningCards: 2 })
  })

  it(
    'карточки слов: новая тема без материалов → список слов → карточки к повторению',
    { timeout: 90_000 },
    async () => {
      // given
      const token = await identityApi.register(`frontend-words-${Date.now()}@example.com`, 'journey-password-123')
      tokenStorage.save(token.accessToken)
      const topic = await topicApi.createTopic('English Unit 5', 'Интеграционный путь карточек слов')

      // when
      const job = await startVocabulary(topic.id, 'contract — договор\nreliable — надёжный')
      await finished(job)
      const cards = await deckApi.listCards(topic.id)
      const due = await eventually(
        () => learningApi.getDueCards(topic.id),
        (list) => list.length === cards.length,
      )

      // then
      expect(cards.length).toBeGreaterThan(0)
      expect(cards[0].front).toContain('_____')
      expect(cards[0].back).toMatch(/^\*\*contract\*\*\n/)
      expect(due).toHaveLength(cards.length)
    },
  )
})

/** Все источники темы обработаны. */
function allReady(sources: Source[]): boolean {
  return sources.every((source) => source.status === 'READY')
}

/** Ждёт, пока загруженное значение не станет готовым, и возвращает его. */
function eventually<T>(load: () => Promise<T>, ready: (value: T) => boolean): Promise<T> {
  return vi.waitFor(
    async () => {
      const value = await load()
      if (!ready(value)) throw new Error('backend ещё обрабатывает запрос')
      return value
    },
    { timeout: TIMEOUT_MS, interval: 500 },
  )
}

/** Запускает карточки слов; сразу после создания темы backend может ещё не знать её владельца — повторяет. */
function startVocabulary(topicId: string, text: string): Promise<GenerationJob> {
  return vi.waitFor(() => deckApi.generateVocabulary(topicId, { text }), { timeout: TIMEOUT_MS, interval: 500 })
}

/** Ждёт завершения задачи генерации и проверяет, что она прошла успешно. */
async function finished(job: GenerationJob): Promise<void> {
  const done = await eventually(
    () => deckApi.getGenerationJob(job.id),
    (current) => current.status !== 'PROCESSING',
  )
  expect(done).toMatchObject({ status: 'READY' })
}
