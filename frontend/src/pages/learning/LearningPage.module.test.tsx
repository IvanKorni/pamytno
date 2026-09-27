import { beforeEach, describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { errorReply, FakeBackend, Reply } from '@/test/fakeBackend'
import { dueCard, progress, session, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

const DUE = '/topics/topic-1/reviews/due?limit=500'
const SESSIONS = '/topics/topic-1/learning-sessions'

describe('Режим обучения', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/sources', [])
    backend.on('POST', SESSIONS, new Reply(201, session()))
    backend.on('POST', '/learning-sessions/session-1/complete', session())
  })

  it('не создаёт сессию, когда повторять нечего', async () => {
    // given
    backend.on('GET', DUE, [])

    // when
    renderApp(backend, '/topics/topic-1/learn')

    // then
    expect(await screen.findByText('Пока нечего повторять')).toBeInTheDocument()
    expect(backend.count('POST', SESSIONS)).toBe(0)
  })

  it('создаёт одну сессию на вход даже в StrictMode', async () => {
    // given
    backend.on('GET', DUE, [dueCard('c-1', 'Что такое MVCC?')])

    // when
    renderApp(backend, '/topics/topic-1/learn')

    // then
    expect(await screen.findByText('Что такое MVCC?')).toBeInTheDocument()
    expect(backend.count('POST', SESSIONS)).toBe(1)
  })

  it('после ухода с экрана и возврата снова показывает свежие карточки', async () => {
    // given
    backend.on('GET', DUE, [dueCard('c-1', 'Что такое MVCC?'), dueCard('c-2', 'Что такое xmin?')])
    backend.on('POST', '/cards/c-1/review', { cardId: 'c-1', result: 'REMEMBER', stage: 1, mastered: false, returnToSession: false })
    renderApp(backend, '/topics/topic-1/learn')
    await answer('Помню')
    backend.on('GET', DUE, [dueCard('c-2', 'Что такое xmin?')])

    // when
    await userEvent.click(screen.getByRole('link', { name: /К теме/ }))
    await userEvent.click(await screen.findByRole('link', { name: /Учить/ }))

    // then
    expect(await screen.findByText('Что такое xmin?')).toBeInTheDocument()
    expect(backend.count('GET', DUE)).toBe(2)
    expect(backend.count('POST', SESSIONS)).toBe(2)
  })

  it('возвращает забытую карточку в конец очереди и завершает сессию', async () => {
    // given
    backend.on('GET', DUE, [dueCard('c-1', 'Что такое MVCC?')])
    backend.on('POST', '/cards/c-1/review', ({ body }: { body?: unknown }) => {
      const { result } = body as { result: string }
      return { cardId: 'c-1', result, stage: 0, mastered: false, returnToSession: result === 'FORGOT' }
    })
    renderApp(backend, '/topics/topic-1/learn')

    // when
    await answer('Не помню')
    await answer('Помню')

    // then
    expect(await screen.findByText('Сессия завершена')).toBeInTheDocument()
    await waitFor(() => expect(backend.count('POST', '/learning-sessions/session-1/complete')).toBe(1))
    expect(backend.calls.filter((call) => call.path === '/api/cards/c-1/review').map((call) => call.body)).toEqual([
      { result: 'FORGOT', sessionId: 'session-1' },
      { result: 'REMEMBER', sessionId: 'session-1' },
    ])
  })

  it('показывает ошибку и повторяет старт по кнопке', async () => {
    // given
    backend.on('GET', DUE, errorReply(500, 'INTERNAL_ERROR', 'Сервис недоступен'))
    renderApp(backend, '/topics/topic-1/learn')
    expect(await screen.findByText('Не удалось начать обучение')).toBeInTheDocument()
    backend.on('GET', DUE, [dueCard('c-1', 'Что такое MVCC?')])

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Попробовать снова' }))

    // then
    expect(await screen.findByText('Что такое MVCC?')).toBeInTheDocument()
  })
})

/** Открывает ответ текущей карточки и оценивает её. */
async function answer(result: 'Помню' | 'Не помню') {
  await userEvent.click(await screen.findByRole('button', { name: /Показать ответ/ }))
  await userEvent.click(screen.getByRole('button', { name: new RegExp(`^\\s*${result}`) }))
}
