import { beforeEach, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { FakeBackend } from '@/test/fakeBackend'
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
    expect(await screen.findByText('Что изучим сегодня?')).toBeInTheDocument()
    expect(screen.getByText('Транзакции PostgreSQL')).toBeInTheDocument()
    expect(screen.getByText(TEST_USER.email)).toBeInTheDocument()
  })

  it('входит по email и паролю и сохраняет токен', async () => {
    // given
    useAuth.setState({ user: undefined })
    backend.on('POST', '/auth/login', { accessToken: 'new-token', tokenType: 'Bearer', expiresIn: 3600, userId: 'u' })
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
    expect(await screen.findByText('Что изучим сегодня?')).toBeInTheDocument()
    expect(localStorage.getItem('pamytno-token')).toBe('new-token')
    expect(backend.calls.find((call) => call.path === '/api/auth/login')?.body).toEqual({
      email: TEST_USER.email,
      password: 'correct-horse',
    })
  })
})
