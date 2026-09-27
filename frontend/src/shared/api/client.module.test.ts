import { beforeEach, describe, expect, it, vi } from 'vitest'
import { request } from './client'

describe('API-клиент', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.stubGlobal('fetch', vi.fn())
  })

  it('передаёт токен и разбирает успешный JSON-ответ', async () => {
    localStorage.setItem('pamytno-token', 'test-token')
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify({ ok: true }), { status: 200 }))

    await expect(request<{ ok: boolean }>('/topics')).resolves.toEqual({ ok: true })
    expect(fetch).toHaveBeenCalledWith('/api/topics', expect.objectContaining({ headers: expect.any(Headers) }))
    const headers = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer test-token')
  })

  it('сообщает серверную ошибку и уведомляет приложение об истёкшем токене', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify({ code: 'UNAUTHORIZED', message: 'Нужен вход' }), { status: 401 }))
    const handler = vi.fn()
    window.addEventListener('pamytno:unauthorized', handler)

    await expect(request('/dashboard')).rejects.toMatchObject({ status: 401, code: 'UNAUTHORIZED', message: 'Нужен вход' })
    expect(handler).toHaveBeenCalledOnce()
    window.removeEventListener('pamytno:unauthorized', handler)
  })
})
