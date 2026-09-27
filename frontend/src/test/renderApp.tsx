import { StrictMode } from 'react'
import { render } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { App } from '@/app/App'
import { useAuth } from '@/modules/identity'
import { tokenStorage } from '@/shared'
import type { FakeBackend } from './fakeBackend'

/** Пользователь, от имени которого работают модульные тесты экранов. */
export const TEST_USER = { id: 'user-1', email: 'student@example.com', createdAt: '2026-09-01T10:00:00Z' }

/**
 * Рендерит всё приложение по адресу от имени вошедшего пользователя — как в браузере, в StrictMode,
 * чтобы ловить двойной запуск эффектов.
 */
export function renderApp(backend: FakeBackend, path: string) {
  tokenStorage.save('test-token')
  useAuth.setState({ user: undefined })
  backend.on('GET', '/users/me', TEST_USER)
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, staleTime: 20_000 } } })
  const view = render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[path]}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>
    </StrictMode>,
  )
  return { ...view, queryClient }
}
