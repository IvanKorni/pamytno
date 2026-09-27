// @vitest-environment node
import ts from 'typescript'
import { describe, expect, it } from 'vitest'
import { allSources, type Source } from './sourceFiles'

/** Кириллица — признак, что описание написано по-русски. */
const CYRILLIC = /[а-яё]/i

/** Объявление верхнего уровня или член класса, которому нужно описание. */
type Documented = ts.Statement | ts.ClassElement

/** Виды объявлений, которым нужно описание: функции, классы, типы, переменные и члены классов. */
const DOCUMENTED: ((node: ts.Node) => boolean)[] = [
  ts.isFunctionDeclaration,
  ts.isClassDeclaration,
  ts.isInterfaceDeclaration,
  ts.isTypeAliasDeclaration,
  ts.isEnumDeclaration,
  ts.isVariableStatement,
  ts.isMethodDeclaration,
  ts.isPropertyDeclaration,
  ts.isConstructorDeclaration,
]

/** Нужно ли объявлению описание; импорты, реэкспорты и выражения описывать не нужно. */
function needsDoc(node: ts.Node): node is Documented {
  return DOCUMENTED.some((isKind) => isKind(node))
}

/** Текст JSDoc-комментария объявления, если он есть. */
function jsDocOf(node: ts.Node, source: Source): string | undefined {
  const text = source.ast.getFullText()
  const comments = ts.getLeadingCommentRanges(text, node.getFullStart()) ?? []
  return comments.map((range) => text.slice(range.pos, range.end)).find((comment) => comment.startsWith('/**'))
}

/** Объявления файла без описания на русском: верхний уровень и члены классов. */
function undocumented(source: Source): string[] {
  const declarations = source.ast.statements.flatMap((node): ts.Node[] =>
    ts.isClassDeclaration(node) ? [node, ...node.members] : [node],
  )
  return declarations
    .filter(needsDoc)
    .filter((node) => !CYRILLIC.test(jsDocOf(node, source) ?? ''))
    .map((node) => `${source.path}:${source.ast.getLineAndCharacterOfPosition(node.getStart()).line + 1}`)
}

describe('описания в коде', () => {
  it('у каждой функции, компонента, класса, типа и константы есть JSDoc на русском', () => {
    // given
    const sources = allSources().filter((source) => !/\.test\.tsx?$/.test(source.path))

    // when
    const missing = sources.flatMap(undocumented)

    // then
    expect(missing).toEqual([])
  })
})
