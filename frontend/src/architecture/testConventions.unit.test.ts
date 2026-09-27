// @vitest-environment node
import ts from 'typescript'
import { describe, expect, it } from 'vitest'
import { importsOf, testSources, walk, type Source } from './sourceFiles'

/** Допустимые уровни тестов — по суффиксу файла. */
const TEST_LEVELS = /\.(unit|module)\.test\.tsx?$|\.integration\.test\.ts$/

/** Кириллица — признак описания на русском. */
const CYRILLIC = /[а-яё]/i

/** Вызовы `describe(...)` и `it(...)` файла. */
function testCalls(source: Source): ts.CallExpression[] {
  const calls: ts.CallExpression[] = []
  walk(source.ast, (node) => {
    if (ts.isCallExpression(node) && /^(describe|it|integration)$/.test(node.expression.getText())) calls.push(node)
  })
  return calls
}

/** Место в файле для сообщения теста. */
const where = (source: Source, node: ts.Node) =>
  `${source.path}:${source.ast.getLineAndCharacterOfPosition(node.getStart()).line + 1}`

/** Описания describe / it файла, написанные не по-русски. */
function englishTitles(source: Source): string[] {
  return testCalls(source)
    .filter((call) => !CYRILLIC.test(call.arguments[0]?.getText() ?? ''))
    .map((call) => where(source, call))
}

/** Тесты файла без блока `// then`. */
function testsWithoutThen(source: Source): string[] {
  return testCalls(source)
    .filter((call) => call.expression.getText() === 'it' && !/\/\/[^\n]*\bthen\b/.test(call.getText()))
    .map((call) => where(source, call))
}

/** Unit-тест, который поднимает приложение или фейковый backend. */
function isHeavyUnitTest(source: Source): boolean {
  return source.path.includes('.unit.test.') && importsOf(source).some((path) => path.startsWith('@/test/'))
}

describe('соглашения тестов', () => {
  const sources = testSources()

  it('файл теста называется по уровню: .unit, .module или .integration', () => {
    // when / then
    expect(sources.map((source) => source.path).filter((path) => !TEST_LEVELS.test(path))).toEqual([])
  })

  it('описания describe и it — на русском', () => {
    // when / then
    expect(sources.flatMap(englishTitles)).toEqual([])
  })

  it('у каждого теста есть блок // then', () => {
    // when / then
    expect(sources.flatMap(testsWithoutThen)).toEqual([])
  })

  it('unit-тесты не поднимают приложение и фейковый backend', () => {
    // when / then
    expect(sources.filter(isHeavyUnitTest).map((source) => source.path)).toEqual([])
  })
})
