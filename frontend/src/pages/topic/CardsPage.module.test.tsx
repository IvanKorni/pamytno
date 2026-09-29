import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { errorReply, FakeBackend, Reply } from '@/test/fakeBackend'
import { CHOICE_TASK, progress, topic } from '@/test/fixtures'
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
    await userEvent.click(screen.getByRole('button', { name: 'Изменить' }))
    await userEvent.click(screen.getByRole('button', { name: 'Удалить' }))

    // then
    expect(await screen.findByText('Здесь пока нет карточек.')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    await waitFor(() => expect(backend.count('GET', '/topics/topic-1/progress')).toBe(2))
  })

  it('показывает ошибку, если карточку не удалось удалить', async () => {
    // given
    backend.on('DELETE', '/cards/card-1', errorReply(404, 'CARD_NOT_FOUND', 'Карточка не найдена'))
    renderApp(backend, '/topics/topic-1/cards')
    await screen.findByText('Что такое MVCC?')

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Изменить' }))
    await userEvent.click(screen.getByRole('button', { name: 'Удалить' }))

    // then
    expect(await screen.findByRole('alert')).toHaveTextContent('Не удалось удалить карточку: Карточка не найдена')
  })

  it('свёрнутая карточка показывает первую строку вопроса, раскрытая — варианты и оформленный ответ', async () => {
    // given
    backend.on('GET', '/topics/topic-1/cards', [{ ...card, ...CHOICE_TASK }])
    renderApp(backend, '/topics/topic-1/cards')
    const toggle = await screen.findByRole('button', { name: /Система держит 100 000 RPS/ })
    expect(screen.queryByText('CQRS и Event Sourcing')).not.toBeInTheDocument()

    // when
    await userEvent.click(toggle)

    // then
    expect(toggle).toHaveAttribute('aria-expanded', 'true')
    expect(screen.getByText('CQRS и Event Sourcing').closest('li')).toHaveTextContent('ACQRS и Event Sourcing')
    expect(screen.getByText('A — CQRS и Event Sourcing.').tagName).toBe('STRONG')
    expect(screen.getByText('Клиент -> Kafka -> PostgreSQL').closest('pre')).not.toBeNull()
  })

  it('у свёрнутой карточки слова видно объяснение и предложение с подсказкой, у раскрытой — слово и перевод', async () => {
    // given
    const word = {
      ...card,
      front: 'Giving help and encouragement to someone.\n\nMy manager was very su_____ at first.',
      back: '**supportive**\nподдерживающий\n\nМой менеджер сначала очень меня поддерживал.',
    }
    backend.on('GET', '/topics/topic-1/cards', [word])
    renderApp(backend, '/topics/topic-1/cards')
    const toggle = await screen.findByRole('button', { name: /Giving help and encouragement/ })
    expect(toggle).toHaveTextContent('My manager was very su_____ at first.')
    expect(screen.queryByText('supportive')).not.toBeInTheDocument()

    // when
    await userEvent.click(toggle)

    // then
    expect(screen.getByText('supportive').tagName).toBe('STRONG')
    expect(screen.getByText(/Мой менеджер сначала/)).toBeInTheDocument()
    expect(screen.getAllByText(/su_____/)).toHaveLength(1)
  })

  it('листает карточки по одной и показывает все сразу', async () => {
    // given
    const second = { ...card, id: 'card-2', front: 'Что такое xmin?', back: 'Транзакция-создатель строки' }
    backend.on('GET', '/topics/topic-1/cards', [card, second])
    renderApp(backend, '/topics/topic-1/cards')
    expect(await screen.findByText('1 / 2')).toBeInTheDocument()
    expect(screen.queryByText('Что такое xmin?')).not.toBeInTheDocument()

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Далее →' }))
    expect(screen.getByText('2 / 2')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Все' }))

    // then
    expect(screen.getByText('Что такое MVCC?')).toBeInTheDocument()
    expect(screen.getByText('Что такое xmin?')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'Изменить' })).toHaveLength(2)
  })
})
