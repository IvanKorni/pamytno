/** Теги, внутри которых нет текста для пользователя: стили и служебная разметка из Word и Google Docs. */
const HIDDEN_TAGS = new Set(['STYLE', 'SCRIPT', 'HEAD', 'META', 'TITLE', 'TEMPLATE'])

/** Блочные теги: их содержимое начинается с новой строки. */
const BLOCK_TAGS = new Set(['P', 'DIV', 'LI', 'TR', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'BLOCKQUOTE', 'TABLE'])

/** Теги, которые браузер выделяет жирным, если стиль не говорит обратного. */
const BOLD_TAGS = new Set(['B', 'STRONG'])

/**
 * Текст из вставленного HTML с жирным выделением в виде `**слово**` — так AI видит, что было выделено
 * в учебнике или на сайте. Возвращает `undefined`, если жирного в HTML нет: тогда браузер вставит текст сам.
 */
export function markBoldText(html: string): string | undefined {
  if (!html) return undefined
  const body = new DOMParser().parseFromString(html, 'text/html').body
  const text = textOf(body, false)
  return text.includes('**') ? tidy(text) : undefined
}

/** Текст узла: пробелы схлопнуты как в браузере; служебные теги и комментарии не дают текста. */
function textOf(node: Node, insideBold: boolean): string {
  if (node.nodeType === Node.TEXT_NODE) return (node.textContent ?? '').replace(/\s+/g, ' ')
  if (!(node instanceof HTMLElement) || HIDDEN_TAGS.has(node.tagName)) return ''
  return elementText(node, insideBold)
}

/** Текст элемента: `<br>` — перенос, блоки — с новой строки, жирное — в `**` без вложенных повторов. */
function elementText(element: HTMLElement, insideBold: boolean): string {
  if (element.tagName === 'BR') return '\n'
  const bold = !insideBold && isBold(element)
  const inner = Array.from(element.childNodes, (child) => textOf(child, insideBold || bold)).join('')
  const text = bold ? wrapBold(inner) : inner
  return BLOCK_TAGS.has(element.tagName) ? `\n${text}\n` : text
}

/**
 * Выделен ли элемент жирным: явный `font-weight` главнее тега — Google Docs оборачивает весь текст
 * в `<b style="font-weight:normal">`.
 */
function isBold(element: HTMLElement): boolean {
  const weight = element.style.fontWeight
  if (weight) return weight === 'bold' || weight === 'bolder' || Number(weight) >= 600
  return BOLD_TAGS.has(element.tagName)
}

/** Оборачивает текст в `**`, оставляя пробелы по краям снаружи; пустое выделение не оборачивается. */
function wrapBold(text: string): string {
  const core = text.trim()
  if (!core) return text
  const start = text.indexOf(core)
  return `${text.slice(0, start)}**${core}**${text.slice(start + core.length)}`
}

/** Убирает пробелы по краям строк и лишние пустые строки. */
function tidy(text: string): string {
  return text
    .split('\n')
    .map((line) => line.trim())
    .join('\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}
