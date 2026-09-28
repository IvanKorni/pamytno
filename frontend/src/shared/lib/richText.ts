/** Вид списка: маркированный, нумерованный или варианты ответа задачи. */
export type RichListKind = 'bullets' | 'numbers' | 'options'

/** Пункт списка: маркер из исходного текста (буква варианта, номер, «-») и текст пункта. */
export interface RichItem {
  marker: string
  text: string
}

/** Блок форматированного текста карточки или вопроса. */
export type RichBlock =
  { kind: 'paragraph'; text: string } | { kind: 'code'; text: string } | { kind: RichListKind; items: RichItem[] }

/** Фрагмент строки: обычный текст, выделение или код. */
export interface RichSpan {
  kind: 'text' | 'bold' | 'code'
  text: string
}

/** Вид строки исходного текста. */
type LineKind = 'blank' | 'fence' | 'text' | RichListKind

/** Шаблоны пунктов списков: первая группа — маркер, вторая — текст пункта. */
const LIST_PATTERNS: Record<RichListKind, RegExp> = {
  options: /^\s*([A-FА-Е])[.)]\s+(.+)$/,
  bullets: /^\s*([-*•])\s+(.+)$/,
  numbers: /^\s*(\d{1,3})[.)]\s+(.+)$/,
}

/** Порядок проверки строки: вариант «A. …» раньше остальных списков. */
const LIST_KINDS: RichListKind[] = ['options', 'bullets', 'numbers']

/** Выделение `**так**` и код `` `так` `` внутри строки; скобки — чтобы split вернул и сами фрагменты. */
const INLINE = /(\*\*[^*\n]+\*\*|`[^`\n]+`)/

/** Длина, до которой однострочный текст крупно выводится заголовком, а длиннее — читается как обычный текст. */
const SHORT_TEXT_LIMIT = 140

/**
 * Разбирает текст карточки на блоки: абзацы, списки, варианты ответа и код в ```.
 * Это небольшое подмножество Markdown, которым AI оформляет ответы.
 */
export function parseRichText(source: string): RichBlock[] {
  const lines = source.replace(/\r\n?/g, '\n').split('\n')
  const blocks: RichBlock[] = []
  let index = 0
  while (index < lines.length) {
    const [block, next] = readBlock(lines, index)
    if (block) blocks.push(block)
    index = next
  }
  return blocks
}

/** Разбирает строку на обычный текст, выделение и код. */
export function parseInline(text: string): RichSpan[] {
  return text.split(INLINE).flatMap((part, index): RichSpan[] => {
    if (!part) return []
    if (index % 2 === 0) return [{ kind: 'text', text: part }]
    return part.startsWith('`')
      ? [{ kind: 'code', text: part.slice(1, -1) }]
      : [{ kind: 'bold', text: part.slice(2, -2) }]
  })
}

/** Текст без разметки выделения — для подписей, где форматирование не нужно. */
export function plainText(text: string): string {
  return parseInline(text)
    .map((span) => span.text)
    .join('')
}

/** Короткий ли текст: одна строка не длиннее {@link SHORT_TEXT_LIMIT} символов — такой вопрос выводится крупно. */
export function isShortText(text: string): boolean {
  const trimmed = text.trim()
  return trimmed.length <= SHORT_TEXT_LIMIT && !trimmed.includes('\n')
}

/** Делит текст на первую строку и остальное: свёрнутая карточка в списке показывает только первую строку. */
export function splitFirstLine(text: string): { first: string; rest: string } {
  const trimmed = text.trim()
  const end = trimmed.indexOf('\n')
  if (end === -1) return { first: trimmed, rest: '' }
  return { first: trimmed.slice(0, end).trim(), rest: trimmed.slice(end + 1).trim() }
}

/** Читает блок, начинающийся со строки `start`, и возвращает его вместе с позицией следующего. */
function readBlock(lines: string[], start: number): [RichBlock | undefined, number] {
  const kind = lineKind(lines[start])
  if (kind === 'blank') return [undefined, start + 1]
  if (kind === 'fence') return readCode(lines, start)
  if (kind === 'text') return readParagraph(lines, start)
  return readList(lines, start, kind)
}

/** Определяет вид строки. */
function lineKind(line: string): LineKind {
  if (!line.trim()) return 'blank'
  if (line.trimStart().startsWith('```')) return 'fence'
  return LIST_KINDS.find((kind) => LIST_PATTERNS[kind].test(line)) ?? 'text'
}

/** Читает код до закрывающих ``` или до конца текста, если модель их забыла. */
function readCode(lines: string[], start: number): [RichBlock, number] {
  const end = lines.findIndex((line, index) => index > start && lineKind(line) === 'fence')
  const stop = end === -1 ? lines.length : end
  return [{ kind: 'code', text: lines.slice(start + 1, stop).join('\n') }, stop + 1]
}

/** Читает абзац: строки текста подряд до пустой строки, списка или кода. */
function readParagraph(lines: string[], start: number): [RichBlock, number] {
  let end = start + 1
  while (end < lines.length && lineKind(lines[end]) === 'text') end++
  const text = lines
    .slice(start, end)
    .map((line) => line.trim())
    .join('\n')
  return [{ kind: 'paragraph', text }, end]
}

/** Читает список одного вида; пустые строки между пунктами его не разрывают. */
function readList(lines: string[], start: number, kind: RichListKind): [RichBlock, number] {
  const items: RichItem[] = []
  let index = start
  while (index < lines.length && lineKind(lines[index]) === kind) {
    items.push(listItem(lines[index], kind))
    index = skipBlankInsideList(lines, index + 1, kind)
  }
  return [listBlock(kind, items), index]
}

/** Пропускает пустые строки, только если за ними продолжается тот же список. */
function skipBlankInsideList(lines: string[], index: number, kind: RichListKind): number {
  let next = index
  while (next < lines.length && lineKind(lines[next]) === 'blank') next++
  return next < lines.length && lineKind(lines[next]) === kind ? next : index
}

/** Пункт списка из строки. */
function listItem(line: string, kind: RichListKind): RichItem {
  const match = LIST_PATTERNS[kind].exec(line)
  return { marker: match?.[1] ?? '', text: match?.[2]?.trim() ?? line.trim() }
}

/** Собирает список; одна строка «A. …» — не варианты ответа, а обычный абзац (например, инициалы). */
function listBlock(kind: RichListKind, items: RichItem[]): RichBlock {
  if (kind === 'options' && items.length < 2) return { kind: 'paragraph', text: `${items[0].marker}. ${items[0].text}` }
  return { kind, items }
}
