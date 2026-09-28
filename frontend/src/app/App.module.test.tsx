import { beforeEach, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { errorReply, FakeBackend } from '@/test/fakeBackend'
import { progress, topic } from '@/test/fixtures'
import { renderApp, TEST_USER } from '@/test/renderApp'
import { useAuth } from '@/modules/identity'
import { App } from './App'

describe('Приложение', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics', [topic()])
    backend.on('GET', '/dashboard', { ...progress(), topics: [progress()] })
  })

  it('восстанавливает вход по сохранённому токену и показывает обзор', async () => {
    // given / when
    renderApp(backend, '/')

    // then
    expect(await screen.findByRole('heading', { name: 'Темы' })).toBeInTheDocument()
    expect(screen.getByText('Транзакции PostgreSQL')).toBeInTheDocument()
    expect(screen.getByText(TEST_USER.email)).toBeInTheDocument()
  })

  it('входит по email и паролю и сохраняет токен', async () => {
    // given
    useAuth.setState({ user: undefined })
    backend.on('POST', '/auth/login', {
      accessToken: 'new-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      userId: 'u',
    })
    backend.on('GET', '/users/me', TEST_USER)
    render(
      <QueryClientProvider client={new QueryClient()}>
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>,
    )

    // when
    await userEvent.type(screen.getByLabelText('Электронная почта'), TEST_USER.email)
    await userEvent.type(screen.getByLabelText('Пароль'), 'correct-horse')
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }))

    // then
    expect(await screen.findByRole('heading', { name: 'Темы' })).toBeInTheDocument()
    expect(localStorage.getItem('pamytno-token')).toBe('new-token')
    expect(backend.calls.find((call) => call.path === '/api/auth/login')?.body).toEqual({
      email: TEST_USER.email,
      password: 'correct-horse',
    })
  })

  it('при выходе очищает кеш, и следующий пользователь не видит чужие темы', async () => {
    // given
    renderApp(backend, '/')
    expect(await screen.findByText('Транзакции PostgreSQL')).toBeInTheDocument()
    backend.on('POST', '/auth/login', {
      accessToken: 'other-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      userId: 'u2',
    })
    backend.on('GET', '/users/me', { ...TEST_USER, id: 'u2', email: 'other@example.com' })
    backend.on('GET', '/topics', [])
    backend.on('GET', '/dashboard', { ...progress('', { totalCards: 0, dueCards: 0, dueToday: 0 }), topics: [] })

    // when
    await userEvent.click(screen.getByRole('button', { name: /Выйти/ }))
    await userEvent.type(await screen.findByLabelText('Электронная почта'), 'other@example.com')
    await userEvent.type(screen.getByLabelText('Пароль'), 'other-password')
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }))

    // then
    expect(await screen.findByText('Здесь пока пусто')).toBeInTheDocument()
    expect(screen.queryByText('Транзакции PostgreSQL')).not.toBeInTheDocument()
  })

  it('после входа возвращает на адрес, который открывали без входа', async () => {
    // given
    backend.on('POST', '/auth/login', {
      accessToken: 'new-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      userId: 'u',
    })
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/cards', [])
    renderApp(backend, '/topics/topic-1/cards', { signedIn: false })

    // when
    await userEvent.type(await screen.findByLabelText('Электронная почта'), TEST_USER.email)
    await userEvent.type(screen.getByLabelText('Пароль'), 'correct-horse')
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }))

    // then
    expect(await screen.findByText('Здесь пока нет карточек.')).toBeInTheDocument()
  })

  it('после «Выйти» следующий пользователь попадает на обзор, а не на страницу предыдущего', async () => {
    // given
    backend.on('GET', '/topics/topic-1', topic())
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/cards', [])
    backend.on('POST', '/auth/login', {
      accessToken: 'other-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      userId: 'u2',
    })
    renderApp(backend, '/topics/topic-1/cards')
    await screen.findByText('Здесь пока нет карточек.')

    // when
    await userEvent.click(screen.getByRole('button', { name: /Выйти/ }))
    await userEvent.type(await screen.findByLabelText('Электронная почта'), 'other@example.com')
    await userEvent.type(screen.getByLabelText('Пароль'), 'other-password')
    await userEvent.click(screen.getByRole('button', { name: 'Войти' }))

    // then
    expect(await screen.findByRole('heading', { name: 'Темы' })).toBeInTheDocument()
  })

  it('при недоступном сервере на старте не выходит, а предлагает повторить', async () => {
    // given
    renderApp(backend, '/', { me: errorReply(503, 'UNAVAILABLE', 'Сервис недоступен') })
    expect(await screen.findByText('Не удалось связаться с сервером')).toBeInTheDocument()
    backend.on('GET', '/users/me', TEST_USER)

    // when
    await userEvent.click(screen.getByRole('button', { name: 'Попробовать снова' }))

    // then
    expect(await screen.findByRole('heading', { name: 'Темы' })).toBeInTheDocument()
    expect(localStorage.getItem('pamytno-token')).toBe('test-token')
  })
})
