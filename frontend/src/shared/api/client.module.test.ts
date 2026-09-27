import { beforeEach, describe, expect, it, vi } from 'vitest'
import { request, UNAUTHORIZED_EVENT } from './client'
import { tokenStorage } from './tokenStorage'

describe('API-клиент', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('передаёт токен и разбирает успешный JSON-ответ', async () => {
    // given
    tokenStorage.save('test-token')
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify({ ok: true }), { status: 200 }))

    // when
    const body = await request<{ ok: boolean }>('/topics')

    // then
    expect(body).toEqual({ ok: true })
    expect(fetch).toHaveBeenCalledWith('/api/topics', expect.objectContaining({ headers: expect.any(Headers) }))
    const headers = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer test-token')
  })

  it('сообщает серверную ошибку и уведомляет приложение об истёкшем токене', async () => {
    // given
    const body = JSON.stringify({ code: 'UNAUTHORIZED', message: 'Нужен вход' })
    vi.mocked(fetch).mockResolvedValue(new Response(body, { status: 401 }))
    const handler = vi.fn()
    window.addEventListener(UNAUTHORIZED_EVENT, handler)

    // when
    const result = request('/dashboard')

    // then
    await expect(result).rejects.toMatchObject({ status: 401, code: 'UNAUTHORIZED', message: 'Нужен вход' })
    expect(handler).toHaveBeenCalledOnce()
    window.removeEventListener(UNAUTHORIZED_EVENT, handler)
  })

  it('не выставляет JSON-заголовок для формы с файлом', async () => {
    // given
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 204 }))
    const form = new FormData()

    // when
    await request('/topics/t-1/sources/pdf', { method: 'POST', body: form })

    // then
    const headers = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers
    expect(headers.has('Content-Type')).toBe(false)
  })
})
