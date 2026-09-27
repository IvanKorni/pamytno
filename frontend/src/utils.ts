export function formatPercent(value: number) {
  return `${Number(value || 0).toLocaleString('ru-RU', { maximumFractionDigits: 1 })}%`
}

export function plural(value: number, one: string, few: string, many: string) {
  const mod10 = value % 10
  const mod100 = value % 100
  return mod10 === 1 && mod100 !== 11 ? one : mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20) ? few : many
}
