import { Fragment, useMemo } from 'react'
import { parseInline, parseRichText, type RichBlock, type RichItem } from '../lib/richText'

export { isShortText, plainText, splitFirstLine } from '../lib/richText'

/** Свойства форматированного текста. */
interface RichTextProps {
  text: string
  className?: string
}

/**
 * Текст карточки или вопроса с простой разметкой: абзацы, списки, варианты ответа, код и выделение.
 * Собирается из React-элементов, а не из HTML, поэтому текст от AI не может внедрить свою разметку.
 */
export function RichText({ text, className = '' }: RichTextProps) {
  const blocks = useMemo(() => parseRichText(text), [text])
  return (
    <div className={`rich-text ${className}`.trim()}>
      {blocks.map((block, index) => (
        <Block key={index} block={block} />
      ))}
    </div>
  )
}

/** Строка с выделением и кодом — для заголовков, внутри которых нельзя размещать абзацы и списки. */
export function RichInline({ text }: { text: string }) {
  return (
    <>
      {parseInline(text).map((span, index) => {
        if (span.kind === 'bold') return <strong key={index}>{span.text}</strong>
        if (span.kind === 'code') return <code key={index}>{span.text}</code>
        return <Fragment key={index}>{span.text}</Fragment>
      })}
    </>
  )
}

/** Один блок текста. */
function Block({ block }: { block: RichBlock }) {
  if (block.kind === 'code') {
    return (
      <pre>
        <code>{block.text}</code>
      </pre>
    )
  }
  if (block.kind === 'paragraph') {
    return (
      <p>
        <RichInline text={block.text} />
      </p>
    )
  }
  if (block.kind === 'options') return <Options items={block.items} />
  const List = block.kind === 'numbers' ? 'ol' : 'ul'
  return (
    <List>
      {block.items.map((item, index) => (
        <li key={index}>
          <RichInline text={item.text} />
        </li>
      ))}
    </List>
  )
}

/** Варианты ответа задачи: буква — отдельным значком, чтобы варианты было легко сравнивать глазами. */
function Options({ items }: { items: RichItem[] }) {
  return (
    <ol className="rich-options">
      {items.map((item, index) => (
        <li key={index}>
          <span className="rich-option-letter">{item.marker}</span>
          <span>
            <RichInline text={item.text} />
          </span>
        </li>
      ))}
    </ol>
  )
}
