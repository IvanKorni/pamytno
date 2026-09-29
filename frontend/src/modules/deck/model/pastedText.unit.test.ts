import { describe, expect, it } from 'vitest'
import { markBoldText } from './pastedText'

describe('Текст из вставленного HTML', () => {
  it('отмечает жирное звёздочками и оставляет пробелы снаружи выделения', () => {
    // given
    const html = '<p>We signed a <b>contract </b>and it was <strong>reliable</strong>.</p>'

    // when
    const text = markBoldText(html)

    // then
    expect(text).toBe('We signed a **contract** and it was **reliable**.')
  })

  it('понимает жирное из стиля и не считает жирной обёртку Google Docs с font-weight:normal', () => {
    // given
    const html =
      '<b style="font-weight:normal" id="docs-internal-guid"><p><span>I had to </span>' +
      '<span style="font-weight:700">make a decision</span><span> quickly.</span></p></b>'

    // when
    const text = markBoldText(html)

    // then
    expect(text).toBe('I had to **make a decision** quickly.')
  })

  it('делит абзацы и строки переносами, схлопывает пробелы и не повторяет вложенное выделение', () => {
    // given
    const html = '<style>p { color: red }</style><p>First   <b><b>line</b></b></p>\n<div>Second<br>third</div>'

    // when
    const text = markBoldText(html)

    // then
    expect(text).toBe('First **line**\n\nSecond\nthird')
  })

  it('без жирного и без HTML ничего не меняет: браузер вставит текст сам', () => {
    // when
    const plain = markBoldText('<p>Just <i>text</i></p>')
    const empty = markBoldText('')

    // then
    expect(plain).toBeUndefined()
    expect(empty).toBeUndefined()
  })
})
