import { beforeEach, describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FakeBackend, Reply } from '@/test/fakeBackend'
import { dueCard, progress, session, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

describe('Обзор', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics', [topic('topic-1', 'Позже сегодня'), topic('topic-2', 'Уже пора')])
  })

  it('начинает повторение с темы, где карточки ждут уже сейчас', async () => {
    // given
    const later = progress('topic-1', { dueCards: 0, dueToday: 3 })
    const now = progress('topic-2', { dueCards: 1, dueToday: 1 })
    backend.on('GET', '/dashboard', { ...progress('', { dueCards: 1, dueToday: 4 }), topics: [later, now] })
    backend.on('GET', '/topics/topic-2', topic('topic-2', 'Уже пора'))
    backend.on('GET', '/topics/topic-2/reviews/due?limit=500', [dueCard('c-1', 'Что такое MVCC?', 'topic-2')])
    backend.on('POST', '/topics/topic-2/learning-sessions', session('session-2', 'topic-2'))
    renderApp(backend, '/')
    expect(await screen.findByText('1 карточка к повторению')).toBeInTheDocument()
    expect(screen.getByText('Самое время освежить то, что уже начинали.')).toBeInTheDocument()

    // when
    await userEvent.click(screen.getByRole('button', { name: /Начать/ }))

    // then
    expect(await screen.findByText('Что такое MVCC?')).toBeInTheDocument()
  })

  it('не предлагает начать, когда карточки подойдут только позже сегодня', async () => {
    // given
    const later = progress('topic-1', { dueCards: 0, dueToday: 3 })
    backend.on('GET', '/dashboard', { ...progress('', { dueCards: 0, dueToday: 3 }), topics: [later] })

    // when
    renderApp(backend, '/')

    // then
    expect(await screen.findByText('0 карточек к повторению')).toBeInTheDocument()
    expect(screen.getByText('Ещё 3 карточки подойдут до конца дня.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Начать/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Повторить/ })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Позже сегодня' })).toHaveAttribute('href', '/topics/topic-1')
  })

  it('создаёт тему в окне и открывает её', async () => {
    // given
    backend.on('GET', '/dashboard', { ...progress('', { dueCards: 0, dueToday: 0 }), topics: [] })
    backend.on('POST', '/topics', new Reply(201, topic('topic-3', 'Kafka')))
    backend.on('GET', '/topics/topic-3', topic('topic-3', 'Kafka'))
    backend.on('GET', '/topics/topic-3/progress', progress('topic-3', { totalCards: 0 }))
    backend.on('GET', '/topics/topic-3/sources', [])
    renderApp(backend, '/')
    await userEvent.click(await screen.findByRole('button', { name: '+ Новая тема' }))

    // when
    await userEvent.type(screen.getByLabelText('Название'), 'Kafka')
    await userEvent.click(screen.getByRole('button', { name: 'Создать тему' }))

    // then
    expect(await screen.findByRole('heading', { level: 1, name: 'Kafka' })).toBeInTheDocument()
    expect(backend.calls.find((call) => call.method === 'POST' && call.path === '/api/topics')?.body).toEqual({
      title: 'Kafka',
    })
  })
})
