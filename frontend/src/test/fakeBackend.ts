import { vi } from 'vitest'

/** Запрос, который приложение отправило в фейковый backend. */
export interface RecordedCall {
  method: string
  path: string
  body?: unknown
}

/** Ответ фейкового backend. */
export interface Reply {
  status?: number
  body?: unknown
}

/** Обработчик маршрута: по запросу возвращает ответ, в том числе асинхронно. */
type Handler = (call: RecordedCall) => Reply | Promise<Reply>

/**
 * Фейковый backend для модульных тестов экранов: подменяет `fetch`, отвечает по таблице маршрутов
 * и запоминает все запросы. Неизвестный маршрут отвечает 404, чтобы тест падал громко.
 */
export class FakeBackend {
  /** Все запросы в порядке отправки. */
  readonly calls: RecordedCall[] = []
  /** Обработчики по ключу `METHOD /path`; последний добавленный главнее. */
  private readonly routes = new Map<string, Handler>()

  /** Подменяет глобальный `fetch` этим backend. */
  install(): this {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: string, init?: RequestInit) => this.handle(input, init)),
    )
    return this
  }

  /** Отвечает на маршрут JSON-телом со статусом 200 или заданным ответом/обработчиком. */
  on(method: string, path: string, reply: Handler | Reply | unknown): this {
    const handler: Handler = typeof reply === 'function' ? (reply as Handler) : () => asReply(reply)
    this.routes.set(`${method} /api${path}`, handler)
    return this
  }

  /** Сколько раз приложение обратилось к маршруту. */
  count(method: string, path: string): number {
    return this.calls.filter((call) => call.method === method && call.path === `/api${path}`).length
  }

  /** Обрабатывает запрос приложения. */
  private async handle(input: string, init?: RequestInit): Promise<Response> {
    const call = { method: init?.method ?? 'GET', path: input, body: parseBody(init?.body) }
    this.calls.push(call)
    const handler = this.routes.get(`${call.method} ${call.path}`)
    const reply = handler ? await handler(call) : { status: 404, body: { code: 'NOT_FOUND', message: call.path } }
    const status = reply.status ?? 200
    return new Response(status === 204 ? null : JSON.stringify(reply.body ?? null), { status })
  }
}

/** Ошибка backend в формате `ErrorResponse`. */
export function errorReply(status: number, code: string, message: string): Reply {
  return { status, body: { code, message, timestamp: '2026-09-27T10:00:00Z' } }
}

/** Превращает значение в ответ: `Reply` остаётся как есть, остальное становится телом 200. */
function asReply(value: unknown): Reply {
  const isReply = typeof value === 'object' && value !== null && ('status' in value || 'body' in value)
  return isReply ? (value as Reply) : { status: 200, body: value }
}

/** Разбирает JSON-тело запроса; форму и пустое тело оставляет как есть. */
function parseBody(body?: BodyInit | null): unknown {
  return typeof body === 'string' ? JSON.parse(body) : (body ?? undefined)
}
