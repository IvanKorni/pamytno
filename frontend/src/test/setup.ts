import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'
import { useAuth } from '@/modules/identity'
import { useToast } from '@/shared'

afterEach(() => {
  if (typeof window === 'undefined') return
  cleanup()
  localStorage.clear()
  sessionStorage.clear()
  useAuth.setState({ user: undefined })
  useToast.setState({ toast: undefined })
})
