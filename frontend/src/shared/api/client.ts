import { tokenStorage } from './tokenStorage'

/** Базовый адрес API: по умолчанию `/api`, который в dev-режиме Vite проксирует на backend. */
function apiUrl(): string {
  return (import.meta.env.VITE_API_URL as string | undefined) || '/api'
}

/** Событие окна: backend ответил 401, приложение должно разлогинить пользователя. */
export const UNAUTHORIZED_EVENT = 'pamytno:unauthorized'

/** Тело ошибки backend — единый `ErrorResponse` из common.error; тела может не быть, поэтому поля необязательны. */
export interface ApiError {
  code?: string
  message?: string
  timestamp?: string
}

/** Ошибка HTTP-запроса со статусом и стабильным кодом ошибки backend. */
export class HttpError extends Error {
  /** HTTP-статус ответа. */
  readonly status: number
  /** Стабильный код ошибки backend, например `TOPIC_NOT_FOUND`. */
  readonly code?: string

  /** Создаёт ошибку из статуса и тела ответа; без сообщения от backend подставляет общее. */
  constructor(status: number, error: ApiError) {
    super(error.message || 'Не удалось выполнить запрос')
    this.name = 'HttpError'
    this.status = status
    this.code = error.code
  }
}

/** Выполняет запрос к API с токеном пользователя и возвращает разобранный JSON-ответ. */
export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${apiUrl()}${path}`, { ...options, headers: buildHeaders(options) })
  if (response.status === 204) return undefined as T
  const payload = parseJson(await response.text())
  if (!response.ok) {
    if (response.status === 401) window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    throw new HttpError(response.status, (payload as ApiError | undefined) ?? {})
  }
  return payload as T
}

/** Формирует параметры запроса с JSON-телом. */
export function json(method: string, body?: unknown): RequestInit {
  return { method, body: body === undefined ? undefined : JSON.stringify(body) }
}

/** Собирает заголовки: JSON для тела, если это не форма, и токен авторизации, если он есть. */
function buildHeaders(options: RequestInit): Headers {
  const headers = new Headers(options.headers)
  const isForm = options.body instanceof FormData
  if (!headers.has('Content-Type') && options.body && !isForm) headers.set('Content-Type', 'application/json')
  const token = tokenStorage.get()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  return headers
}

/** Разбирает текст ответа как JSON; пустое или некорректное тело даёт `undefined`. */
function parseJson(text: string): unknown {
  if (!text) return undefined
  try {
    return JSON.parse(text)
  } catch {
    return undefined
  }
}
