import { QueryClient } from '@tanstack/react-query'
import { HttpError } from '@/shared'

/** Сколько раз повторять упавший запрос при сетевой ошибке или ошибке сервера. */
const MAX_RETRIES = 1

/**
 * Создаёт кеш запросов: данные свежи 20 секунд; сетевые ошибки и 5xx повторяются один раз,
 * а ответы 4xx — нет: «не найдено» или «нет доступа» от повтора не изменятся.
 */
export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 20_000,
        retry: (failures, error) => !isClientError(error) && failures < MAX_RETRIES,
      },
    },
  })
}

/** Ошибка из-за самого запроса (4xx), а не из-за сети или сервера. */
function isClientError(error: unknown): boolean {
  return error instanceof HttpError && error.status >= 400 && error.status < 500
}
