import { Link, NavLink, Outlet, useMatch } from 'react-router-dom'
import { useAuth, useLogout } from '@/modules/identity'
import { useDashboard } from '@/modules/learning'
import { Brand, ToastContainer } from '@/shared'
import { useScrolled } from './useScrolled'

/**
 * Каркас приложения для вошедшего пользователя: шапка, экран и уведомления.
 * Режим обучения занимает весь экран — без шапки, чтобы ничто не отвлекало от карточки.
 */
export function AppShell() {
  const learning = useMatch('/topics/:topicId/learn')
  return (
    <div className="app-shell">
      {!learning && <AppHeader />}
      <main className={learning ? 'learning-main' : 'page'}>
        <Outlet />
      </main>
      <ToastContainer />
    </div>
  )
}

/** Шапка: логотип, раздел «Темы», сколько повторить сегодня, пользователь и выход. */
function AppHeader() {
  const scrolled = useScrolled()
  const user = useAuth((state) => state.user)
  const logout = useLogout()
  return (
    <header className={scrolled ? 'app-header is-scrolled' : 'app-header'}>
      <div className="header-inner">
        <nav className="header-nav" aria-label="Разделы">
          <Link to="/" className="brand-link">
            <Brand />
          </Link>
          <NavLink to="/" end className="header-link">
            Темы
          </NavLink>
        </nav>
        <div className="header-side">
          <DueToday />
          <span className="header-user">{user?.email}</span>
          <button className="button button-secondary button-compact" onClick={() => logout('signOut')}>
            Выйти
          </button>
        </div>
      </div>
    </header>
  )
}

/** Сколько карточек подойдёт к повторению до конца дня по всем темам. */
function DueToday() {
  const dashboard = useDashboard()
  if (!dashboard.data) return null
  return <span className="mono header-due">{dashboard.data.dueToday} на сегодня</span>
}
