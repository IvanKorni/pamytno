import { Navigate, Route, Routes } from 'react-router-dom'
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

/** Корень приложения: восстанавливает вход и раздаёт экраны по адресам. */
export function App() {
  const booting = useAuthBootstrap()
  const user = useAuth((state) => state.user)
  if (booting) return <PageLoading text="Загружаем Памятно…" />
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <AuthPage mode="login" />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <AuthPage mode="register" />} />
      <Route element={user ? <AppShell /> : <Navigate to="/login" replace />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/topics/new" element={<CreateTopicPage />} />
        <Route path="/topics/:topicId/learn" element={<LearningPage />} />
        <Route path="/topics/:topicId" element={<TopicLayout />}>
          <Route index element={<MaterialsPage />} />
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
