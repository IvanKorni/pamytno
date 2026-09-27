import { vi } from 'vitest'

/** Запрос, который приложение отправило в фейковый backend. */
export interface RecordedCall {
  method: string
  path: string
  body?: unknown
}

/** Ответ фейкового backend с явным статусом; всё остальное считается телом ответа 200. */
export class Reply {
  /** Создаёт ответ со статусом и телом. */
  constructor(
    readonly status: number,
    readonly body?: unknown,
  ) {}
}

/** Обработчик маршрута: по запросу возвращает тело или `Reply`, в том числе асинхронно. */
type Handler = (call: RecordedCall) => unknown

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

  /** Отвечает на маршрут телом со статусом 200, заданным `Reply` или результатом обработчика. */
  on(method: string, path: string, response: unknown): this {
    const handler: Handler = typeof response === 'function' ? (response as Handler) : () => response
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
    const result = handler ? await handler(call) : errorReply(404, 'NOT_FOUND', call.path)
    const reply = result instanceof Reply ? result : new Reply(200, result)
    const body = reply.status === 204 ? null : JSON.stringify(reply.body ?? null)
    return new Response(body, { status: reply.status })
  }
}

/** Ошибка backend в формате `ErrorResponse`. */
export function errorReply(status: number, code: string, message: string): Reply {
  return new Reply(status, { code, message, timestamp: '2026-09-27T10:00:00Z' })
}

/** Разбирает JSON-тело запроса; форму и пустое тело оставляет как есть. */
function parseBody(body?: BodyInit | null): unknown {
  return typeof body === 'string' ? JSON.parse(body) : (body ?? undefined)
}
