import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { errorReply, FakeBackend, Reply } from '@/test/fakeBackend'
import { progress, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

const source = (status: string) => ({
  id: 'source-1',
  topicId: 'topic-1',
  type: 'TEXT',
  status,
  originalName: 'Конспект',
  createdAt: '2026-09-27T10:00:00Z',
  updatedAt: '2026-09-27T10:00:00Z',
})

describe('Экран материалов', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    vi.spyOn(window, 'confirm').mockReturnValue(true)
  })

  it('добавляет заметки и перечитывает список материалов', async () => {
    // given
    backend.on('GET', '/topics/topic-1/sources', [])
    backend.on('POST', '/topics/topic-1/sources/text', new Reply(202, source('UPLOADED')))
    renderApp(backend, '/topics/topic-1')
    await userEvent.click(await screen.findByRole('button', { name: /Добавить материал/ }))
    backend.on('GET', '/topics/topic-1/sources', [source('READY')])

    // when
    await userEvent.type(screen.getByLabelText('Текст заметок'), 'MVCC — многоверсионность')
    await userEvent.click(screen.getAllByRole('button', { name: 'Добавить материал' }).at(-1)!)

    // then
    expect(await screen.findByText('Конспект')).toBeInTheDocument()
    expect(backend.calls.find((call) => call.path.endsWith('/sources/text'))?.body).toEqual({
      type: 'TEXT',
      name: 'Мои заметки',
      text: 'MVCC — многоверсионность',
    })
  })

  it('показывает ошибку, если материал не удалось удалить', async () => {
    // given
    backend.on('GET', '/topics/topic-1/sources', [source('READY')])
    backend.on('DELETE', '/sources/source-1', errorReply(500, 'INTERNAL_ERROR', 'Сервис недоступен'))
    renderApp(backend, '/topics/topic-1')

    // when
    await userEvent.click(await screen.findByTitle('Удалить материал'))

    // then
    expect(await screen.findByRole('alert')).toHaveTextContent('Не удалось удалить материал: Сервис недоступен')
    await waitFor(() => expect(screen.getByText('Конспект')).toBeInTheDocument())
  })

  it('закрывает окно добавления по Esc и возвращает фокус на кнопку', async () => {
    // given
    backend.on('GET', '/topics/topic-1/sources', [])
    renderApp(backend, '/topics/topic-1')
    const open = await screen.findByRole('button', { name: /Добавить материал/ })
    await userEvent.click(open)
    expect(screen.getByRole('dialog', { name: 'Добавить материал' })).toBeInTheDocument()

    // when
    await userEvent.keyboard('{Escape}')

    // then
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(open).toHaveFocus()
  })

  it('отправляет ссылку YouTube без пробелов по краям', async () => {
    // given
    backend.on('GET', '/topics/topic-1/sources', [])
    backend.on('POST', '/topics/topic-1/sources/youtube', new Reply(202, source('UPLOADED')))
    renderApp(backend, '/topics/topic-1')
    await userEvent.click(await screen.findByRole('button', { name: /Добавить материал/ }))
    await userEvent.click(screen.getByRole('button', { name: /YouTube/ }))

    // when
    await userEvent.type(screen.getByLabelText('YouTube URL'), '  https://youtu.be/abc  ')
    await userEvent.click(screen.getAllByRole('button', { name: 'Добавить материал' }).at(-1)!)

    // then
    await waitFor(() => expect(backend.count('POST', '/topics/topic-1/sources/youtube')).toBe(1))
    expect(backend.calls.find((call) => call.path.endsWith('/sources/youtube'))?.body).toEqual({
      url: 'https://youtu.be/abc',
    })
  })
})
