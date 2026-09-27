import type { ApiError } from '../types'

const API_URL = (import.meta.env.VITE_API_URL as string | undefined) || '/api'

export class HttpError extends Error {
  status: number
  code?: string

  constructor(status: number, error: ApiError) {
    super(error.message || 'Не удалось выполнить запрос')
    this.name = 'HttpError'
    this.status = status
    this.code = error.code
  }
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('pamytno-token')
  const headers = new Headers(options.headers)
  if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  if (response.status === 204) return undefined as T
  const text = await response.text()
  let payload: T | ApiError | undefined
  try { payload = text ? JSON.parse(text) : undefined } catch { payload = undefined }
  if (!response.ok) {
    if (response.status === 401) window.dispatchEvent(new Event('pamytno:unauthorized'))
    throw new HttpError(response.status, (payload as ApiError) || {})
  }
  return payload as T
}

export const json = (method: string, body?: unknown): RequestInit => ({ method, body: body === undefined ? undefined : JSON.stringify(body) })

