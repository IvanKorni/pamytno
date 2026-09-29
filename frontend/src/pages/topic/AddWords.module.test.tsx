import { beforeEach, describe, expect, it } from 'vitest'
import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { errorReply, FakeBackend, Reply } from '@/test/fakeBackend'
import { job, progress, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

/** Карточка слова, как её собирает backend. */
const wordCard = {
  id: 'card-1',
  topicId: 'topic-1',
  front: 'A formal written agreement.\n\nWe signed a _____ yesterday.',
  back: '**contract**\nконтракт, договор\n\nМы вчера подписали контракт.',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
}

/** Задача составления карточек слов. */
const wordsJob = (status: string, itemsCreated = 0) => job('job-1', status, { type: 'VOCABULARY', itemsCreated })

describe('Карточки слов', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/sources', [])
    backend.on('GET', '/topics/topic-1/cards', [])
  })

  it('составляет карточки по жирным словам из вставленного текста и показывает их на вкладке карточек', async () => {
    // given
    backend.on('POST', '/topics/topic-1/vocabulary/generate', new Reply(202, wordsJob('PROCESSING')))
    backend.on('GET', '/generation-jobs/job-1', wordsJob('PROCESSING'))
    renderApp(backend, '/topics/topic-1/materials')
    await userEvent.click(await screen.findByRole('button', { name: 'Добавить слова' }))
    await userEvent.click(screen.getByRole('button', { name: 'Жирные' }))
    fireEvent.paste(screen.getByLabelText('Слова или текст'), {
      clipboardData: { getData: (type: string) => (type === 'text/html' ? '<p>We signed a <b>contract</b>.</p>' : '') },
    })

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Составить карточки' }))

    // then
    expect(await screen.findByText('Составляем карточки слов')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Карточки/ })).toHaveAttribute('aria-current', 'page')
    const started = backend.calls.find((call) => call.method === 'POST')
    expect(started?.body).toEqual({ text: 'We signed a **contract**.', instruction: expect.stringContaining('жирным') })
    backend.on('GET', '/topics/topic-1/cards', [wordCard])
    backend.on('GET', '/generation-jobs/job-1', wordsJob('READY', 1))
    expect(await screen.findByText('A formal written agreement.', {}, { timeout: 3000 })).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Готово: 1 карточка слов')
  })

  it('с пустой вкладки карточек открывает окно слов и показывает, если backend не принял задачу', async () => {
    // given
    backend.on('POST', '/topics/topic-1/vocabulary/generate', errorReply(404, 'TOPIC_NOT_FOUND', 'Тема не найдена'))
    renderApp(backend, '/topics/topic-1/cards')
    await screen.findByText('Здесь пока нет карточек.')
    const [, inEmptyState] = screen.getAllByRole('button', { name: 'Добавить слова' })
    await userEvent.click(inEmptyState)
    await userEvent.type(screen.getByLabelText('Слова или текст'), 'contract')

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Составить карточки' }))

    // then
    expect(await screen.findByText('Тема не найдена')).toBeInTheDocument()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(backend.calls.find((call) => call.method === 'POST')?.body).toEqual({ text: 'contract' })
    await userEvent.click(screen.getByRole('button', { name: 'Закрыть' }))
    await userEvent.click(screen.getAllByRole('button', { name: 'Добавить слова' })[0])
    expect(screen.queryByText('Тема не найдена')).not.toBeInTheDocument()
  })
})
