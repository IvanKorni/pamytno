import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { errorReply, FakeBackend, Reply } from '@/test/fakeBackend'
import { progress, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

const card = {
  id: 'card-1',
  topicId: 'topic-1',
  front: 'Что такое MVCC?',
  back: 'Многоверсионность строк',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
}

describe('Экран карточек', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/cards', [card])
    vi.spyOn(window, 'confirm').mockReturnValue(true)
  })

  it('после удаления карточки перечитывает прогресс темы', async () => {
    // given
    backend.on('DELETE', '/cards/card-1', new Reply(204))
    renderApp(backend, '/topics/topic-1/cards')
    await screen.findByText('Что такое MVCC?')
    backend.on('GET', '/topics/topic-1/cards', [])

    // when
    await userEvent.click(screen.getByTitle('Удалить'))

    // then
    expect(await screen.findByText('Карточек пока нет')).toBeInTheDocument()
    await waitFor(() => expect(backend.count('GET', '/topics/topic-1/progress')).toBe(2))
  })

  it('показывает ошибку, если карточку не удалось удалить', async () => {
    // given
    backend.on('DELETE', '/cards/card-1', errorReply(404, 'CARD_NOT_FOUND', 'Карточка не найдена'))
    renderApp(backend, '/topics/topic-1/cards')
    await screen.findByText('Что такое MVCC?')

    // when
    await userEvent.click(screen.getByTitle('Удалить'))

    // then
    expect(await screen.findByRole('alert')).toHaveTextContent('Не удалось удалить карточку: Карточка не найдена')
  })
})
