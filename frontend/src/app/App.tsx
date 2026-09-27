import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useAuth, useAuthBootstrap } from '@/modules/identity'
import { AuthPage } from '@/pages/AuthPage'
import { CreateTopicPage } from '@/pages/CreateTopicPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { LearningPage } from '@/pages/learning/LearningPage'
import { CardsPage } from '@/pages/topic/CardsPage'
import { MaterialsPage } from '@/pages/topic/MaterialsPage'
import { ProgressPage } from '@/pages/topic/ProgressPage'
import { QuestionsPage } from '@/pages/topic/QuestionsPage'
import { TopicLayout } from '@/pages/topic/TopicLayout'
import { PageLoading } from '@/shared'
import { AppShell } from './AppShell'

/** Куда вернуть пользователя после входа: адрес, который он открывал без входа. */
interface ReturnState {
  from?: string
}

/** Корень приложения: восстанавливает вход и раздаёт экраны по адресам. */
export function App() {
  const booting = useAuthBootstrap()
  if (booting) return <PageLoading text="Загружаем Памятно…" />
  return (
    <Routes>
      <Route path="/login" element={<GuestOnly page={<AuthPage mode="login" />} />} />
      <Route path="/register" element={<GuestOnly page={<AuthPage mode="register" />} />} />
      <Route element={<RequireAuth />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/topics/new" element={<CreateTopicPage />} />
        <Route path="/topics/:topicId/learn" element={<LearningPage />} />
        <Route path="/topics/:topicId" element={<TopicLayout />}>
          <Route index element={<Navigate to="materials" replace />} />
          <Route path="materials" element={<MaterialsPage />} />
          <Route path="questions" element={<QuestionsPage />} />
          <Route path="cards" element={<CardsPage />} />
          <Route path="progress" element={<ProgressPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

/** Пускает только вошедшего пользователя; остальных отправляет на вход, запомнив адрес. */
function RequireAuth() {
  const user = useAuth((state) => state.user)
  const location = useLocation()
  if (user) return <AppShell />
  const from = location.pathname + location.search
  return <Navigate to="/login" replace state={{ from } satisfies ReturnState} />
}

/** Экран для гостя; вошедшего пользователя возвращает туда, откуда его отправили на вход. */
function GuestOnly({ page }: { page: JSX.Element }) {
  const user = useAuth((state) => state.user)
  const location = useLocation()
  if (!user) return page
  const from = (location.state as ReturnState | null)?.from
  return <Navigate to={from ?? '/'} replace />
}
