/**
 * Значения в sessionStorage: переживают переход между экранами и перезагрузку вкладки, но не её закрытие.
 * Хранилище может быть недоступно (приватный режим, запрет сайта) — тогда значения просто не запоминаются.
 */
export const sessionValues = {
  /** Возвращает значение по ключу или `undefined`. */
  get(key: string): string | undefined {
    try {
      return sessionStorage.getItem(key) ?? undefined
    } catch {
      return undefined
    }
  },
  /** Запоминает значение. */
  set(key: string, value: string): void {
    try {
      sessionStorage.setItem(key, value)
    } catch {
      // без хранилища значение живёт только до ухода с экрана
    }
  },
  /** Забывает значение. */
  remove(key: string): void {
    try {
      sessionStorage.removeItem(key)
    } catch {
      // нечего удалять
    }
  },
}
