import { beforeEach, describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import { errorReply, FakeBackend } from '@/test/fakeBackend'
import { progress, topic } from '@/test/fixtures'
import { renderApp } from '@/test/renderApp'

describe('Экран темы', () => {
  let backend: FakeBackend

  beforeEach(() => {
    backend = new FakeBackend().install()
    backend.on('GET', '/topics/topic-1/progress', progress())
    backend.on('GET', '/topics/topic-1/sources', [])
  })

  it('открывает материалы и подсвечивает их вкладку', async () => {
    // given
    backend.on('GET', '/topics/topic-1', topic())

    // when
    renderApp(backend, '/topics/topic-1')

    // then
    expect(await screen.findByText('Здесь пока нет материалов.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Материалы/ })).toHaveAttribute('aria-current', 'page')
  })

  it('говорит, что темы нет, если backend ответил 404', async () => {
    // given
    backend.on('GET', '/topics/topic-1', errorReply(404, 'TOPIC_NOT_FOUND', 'Тема не найдена'))

    // when
    renderApp(backend, '/topics/topic-1')

    // then
    expect(await screen.findByRole('heading', { name: 'Тема не найдена' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'К обзору' })).toHaveAttribute('href', '/')
    expect(backend.count('GET', '/topics/topic-1')).toBe(1)
  })
})
