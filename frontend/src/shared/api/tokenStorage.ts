/** Ключ, под которым access-токен лежит в localStorage. */
const TOKEN_KEY = 'pamytno-token'

/** Хранилище access-токена — единственное место, где фронт обращается к localStorage. */
export const tokenStorage = {
  /** Возвращает сохранённый токен или null, если пользователь не входил. */
  get: (): string | null => localStorage.getItem(TOKEN_KEY),
  /** Сохраняет токен после входа или регистрации. */
  save: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  /** Удаляет токен при выходе или истечении сессии. */
  clear: () => localStorage.removeItem(TOKEN_KEY),
}
