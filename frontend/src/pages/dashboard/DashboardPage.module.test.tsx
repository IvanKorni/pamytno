import { beforeEach, describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FakeBackend } from '@/test/fakeBackend'
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
})
