// @vitest-environment node
import ts from 'typescript'
import { describe, expect, it } from 'vitest'
import { appSources, importsOf, walk, type Source } from './sourceFiles'

/** Больше зависимостей у файла — признак, что он делает слишком много (как fan-out в Checkstyle). */
const MAX_IMPORTS = 15

/** Файлы приложения, где встречается узел, подходящий под условие. */
function filesWith(match: (node: ts.Node) => boolean): string[] {
  return appSources()
    .filter((source: Source) => {
      let found = false
      walk(source.ast, (node) => {
        found ||= match(node)
      })
      return found
    })
    .map((source) => source.path)
}

/** Вызов глобальной функции по имени: `fetch(...)`. */
const isCallOf = (name: string) => (node: ts.Node) =>
  ts.isCallExpression(node) && ts.isIdentifier(node.expression) && node.expression.text === name

/** Обращение к глобальному объекту по имени: `localStorage.getItem`. */
const isGlobal = (name: string) => (node: ts.Node) =>
  ts.isPropertyAccessExpression(node) && ts.isIdentifier(node.expression) && node.expression.text === name

/** Текущее время: `Date.now()` или `new Date()` без аргументов. */
const isCurrentTime = (node: ts.Node) =>
  (ts.isNewExpression(node) && node.expression.getText() === 'Date' && !node.arguments?.length) ||
  (ts.isPropertyAccessExpression(node) && node.getText() === 'Date.now')

/** Вставка строки как HTML: `dangerouslySetInnerHTML={...}`. */
const isRawHtml = (node: ts.Node) => ts.isJsxAttribute(node) && node.name.getText() === 'dangerouslySetInnerHTML'

describe('правила кода', () => {
  it('HTTP-запросы идут только через shared/api/client.ts', () => {
    // when / then
    expect(filesWith(isCallOf('fetch'))).toEqual(['shared/api/client.ts'])
  })

  it('localStorage — только для токена, sessionStorage — только через sessionValues', () => {
    // when / then
    expect(filesWith(isGlobal('localStorage'))).toEqual(['shared/api/tokenStorage.ts'])
    expect(filesWith(isGlobal('sessionStorage'))).toEqual(['shared/lib/sessionValues.ts'])
  })

  it('фронт не берёт текущее время: сроки повторения считает backend', () => {
    // when / then
    expect(filesWith(isCurrentTime)).toEqual([])
  })

  it('текст с backend не вставляется как HTML: разметку карточек строит RichText', () => {
    // when / then
    expect(filesWith(isRawHtml)).toEqual([])
  })

  it(`у файла не больше ${MAX_IMPORTS} импортов`, () => {
    // when
    const heavy = appSources().filter((source) => importsOf(source).length > MAX_IMPORTS)

    // then
    expect(heavy.map((source) => source.path)).toEqual([])
  })
})
