import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, LogOut, Menu, Plus } from 'lucide-react'
import { useAuth, useLogout } from '@/modules/identity'
import { Brand, ToastContainer } from '@/shared'

/** Каркас приложения для вошедшего пользователя: боковое меню, экран и уведомления. */
export function AppShell() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const closeMenu = () => setMobileOpen(false)
  return (
    <div className="app-shell">
      <Sidebar open={mobileOpen} onNavigate={closeMenu} />
      {mobileOpen && <button className="mobile-backdrop" aria-label="Закрыть меню" onClick={closeMenu} />}
      <main className="main-content">
        <button className="mobile-menu" aria-label="Открыть меню" onClick={() => setMobileOpen(true)}>
          <Menu size={22} />
        </button>
        <Outlet />
      </main>
      <ToastContainer />
    </div>
  )
}

/** Боковое меню: разделы, пользователь и выход; на мобильном выезжает поверх экрана. */
function Sidebar({ open, onNavigate }: { open: boolean; onNavigate: () => void }) {
  const user = useAuth((state) => state.user)
  const logout = useLogout()
  return (
    <aside className={`sidebar ${open ? 'is-open' : ''}`}>
      <Brand />
      <nav className="main-nav">
        <NavLink to="/" end onClick={onNavigate}>
          <LayoutDashboard size={18} /> Обзор
        </NavLink>
        <NavLink to="/topics/new" onClick={onNavigate}>
          <Plus size={18} /> Новая тема
        </NavLink>
      </nav>
      <div className="sidebar-bottom">
        <div className="user-chip">
          <span className="avatar">{user?.email.slice(0, 1).toUpperCase()}</span>
          <span className="user-email">{user?.email}</span>
        </div>
        <button className="sidebar-logout" onClick={() => logout('signOut')}>
          <LogOut size={16} /> Выйти
        </button>
      </div>
    </aside>
  )
}
