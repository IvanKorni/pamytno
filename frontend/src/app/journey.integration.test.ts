import { describe, expect, it } from 'vitest'

const baseUrl = import.meta.env.VITE_PAMYTNO_INTEGRATION_URL
const integration = baseUrl ? describe : describe.skip

integration('HTTP-путь frontend → backend', () => {
  it('регистрирует пользователя, создаёт тему и принимает текстовый источник', async () => {
    const suffix = Date.now()
    const email = `frontend-integration-${suffix}@example.com`
    const auth = await fetch(`${baseUrl}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password: 'test-password-123' }),
    })
    expect(auth.status).toBe(201)
    const token = (await auth.json()).accessToken as string
    const topic = await fetch(`${baseUrl}/api/topics`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: 'Frontend integration' }),
    })
    expect(topic.status).toBe(201)
    const topicId = (await topic.json()).id as string
    const source = await fetch(`${baseUrl}/api/topics/${topicId}/sources/text`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: 'TEXT', name: 'Notes', text: 'Материал для интеграционной проверки.' }),
    })
    expect(source.status).toBe(202)
  })
})
