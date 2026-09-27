import type { Source } from './types'

/** Как часто опрашивать источники, пока backend их обрабатывает, мс. */
export const SOURCE_POLL_INTERVAL_MS = 1500

/** Возвращает интервал опроса, пока хотя бы один источник ещё в обработке, иначе `false`. */
export function sourcesPollInterval(sources?: Source[]): number | false {
  const processing = sources?.some((source) => source.status === 'UPLOADED' || source.status === 'PROCESSING')
  return processing ? SOURCE_POLL_INTERVAL_MS : false
}
