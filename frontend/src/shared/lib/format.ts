/** Форматирует процент прогресса по-русски с одним знаком после запятой: `68,2%`. */
export function formatPercent(value: number): string {
  return `${Number(value || 0).toLocaleString('ru-RU', { maximumFractionDigits: 1 })}%`
}

/** Выбирает форму слова для числа: 1 карточка, 2 карточки, 5 карточек. */
export function plural(value: number, one: string, few: string, many: string): string {
  const mod10 = value % 10
  const mod100 = value % 100
  if (mod10 === 1 && mod100 !== 11) return one
  const isFew = mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)
  return isFew ? few : many
}
