import { beforeEach, describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FakeBackend, Reply } from '@/test/fakeBackend'
import { job, progress, question, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

const QUESTIONS = '/topics/topic-1/questions'

describe('Экран вопросов', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/cards', [])
    backend.on('GET', QUESTIONS, [])
  })

  it('показывает ошибку генерации и перестаёт опрашивать задачу', async () => {
    // given
    backend.on('POST', `${QUESTIONS}/generate`, new Reply(202, job('job-1', 'PROCESSING')))
    backend.on('GET', '/generation-jobs/job-1', job('job-1', 'ERROR', { errorMessage: 'AI недоступен' }))
    renderApp(backend, '/topics/topic-1/questions')

    // when
    await userEvent.click(await screen.findByRole('button', { name: /Создать вопросы/ }))

    // then
    expect(await screen.findByText('AI недоступен')).toBeInTheDocument()
    expect(screen.queryByText(/Анализируем материалы/)).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Создать вопросы/ })).toBeEnabled()
    await new Promise((resolve) => setTimeout(resolve, 1500))
    expect(backend.count('GET', '/generation-jobs/job-1')).toBe(1)
  })

  it('продолжает следить за генерацией после ухода с вкладки', async () => {
    // given
    backend.on('POST', `${QUESTIONS}/generate`, new Reply(202, job('job-1', 'PROCESSING')))
    backend.on('GET', '/generation-jobs/job-1', job('job-1', 'PROCESSING', { itemsCreated: 2 }))
    renderApp(backend, '/topics/topic-1/questions')
    await userEvent.click(await screen.findByRole('button', { name: /Создать вопросы/ }))
    await screen.findByText(/Анализируем материалы/)

    // when
    await userEvent.click(screen.getByRole('link', { name: /Карточки/ }))
    backend.on('GET', '/generation-jobs/job-1', job('job-1', 'READY', { itemsCreated: 1 }))
    backend.on('GET', QUESTIONS, [question('q-1', 'Что такое MVCC?')])
    await userEvent.click(screen.getByRole('link', { name: /Вопросы/ }))

    // then
    expect(await screen.findByText('Что такое MVCC?')).toBeInTheDocument()
    expect(backend.count('POST', `${QUESTIONS}/generate`)).toBe(1)
  })

  it('снимает выбор без запросов к backend', async () => {
    // given
    backend.on('GET', QUESTIONS, [question('q-1', 'Что такое MVCC?')])
    renderApp(backend, '/topics/topic-1/questions')
    await userEvent.click(await screen.findByRole('button', { name: 'Списком' }))
    await userEvent.click(screen.getByRole('checkbox'))

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Снять выбор' }))

    // then
    expect(screen.getByRole('checkbox')).not.toBeChecked()
    expect(backend.count('POST', '/questions/decisions')).toBe(0)
  })

  it('не отправляет второе решение, пока первое не вернулось', async () => {
    // given
    let respond: (value: unknown) => void = () => undefined
    backend.on('GET', QUESTIONS, [question('q-1', 'Что такое MVCC?'), question('q-2', 'Что такое xmin?')])
    backend.on('POST', '/questions/q-1/approve', () => new Promise((resolve) => (respond = resolve)))
    renderApp(backend, '/topics/topic-1/questions')
    const approve = await screen.findByRole('button', { name: /Хочу изучить/ })

    // when
    await userEvent.click(approve)
    await userEvent.click(approve)
    respond(question('q-1', 'Что такое MVCC?', 'APPROVED'))

    // then
    expect(await screen.findByText('Что такое xmin?')).toBeInTheDocument()
    expect(screen.getByText('Что такое xmin?').closest('.review-panel')).toHaveTextContent('2 / 2')
    expect(backend.count('POST', '/questions/q-1/approve')).toBe(1)
  })

  it('обещает карточки только после того, как они созданы', async () => {
    // given
    backend.on('GET', QUESTIONS, [question('q-1', 'Что такое MVCC?')])
    backend.on('POST', '/questions/q-1/approve', question('q-1', 'Что такое MVCC?', 'APPROVED'))
    backend.on('POST', '/topics/topic-1/cards/generate', new Reply(202, job('job-2', 'PROCESSING', { type: 'CARDS' })))
    backend.on('GET', '/generation-jobs/job-2', job('job-2', 'READY', { type: 'CARDS', itemsCreated: 1 }))
    renderApp(backend, '/topics/topic-1/questions')
    await userEvent.click(await screen.findByRole('button', { name: /Хочу изучить/ }))
    expect(screen.queryByText(/создан/)).not.toBeInTheDocument()

    // when
    backend.on('GET', QUESTIONS, [question('q-1', 'Что такое MVCC?', 'CARD_CREATED')])
    await userEvent.click(screen.getByRole('button', { name: /Создать карточки/ }))

    // then
    expect(await screen.findByText('1 карточка создана')).toBeInTheDocument()
    await waitFor(() => expect(backend.count('GET', '/topics/topic-1/progress')).toBe(2))
  })
})
