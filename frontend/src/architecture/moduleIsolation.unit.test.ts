// @vitest-environment node
import { describe, expect, it } from 'vitest'
import { allSources, appSources, importsOf, resolveImport, unitOf, type Source } from './sourceFiles'

/** Импорт между файлами: откуда, спецификатор и куда он ведёт. */
interface Dependency {
  from: string
  fromUnit: string
  specifier: string
  target: string
  targetUnit: string
}

/** Все внутренние импорты файлов. */
function dependencies(sources: Source[]): Dependency[] {
  return sources.flatMap((source) =>
    importsOf(source).flatMap((specifier) => {
      const target = resolveImport(source, specifier)
      if (!target) return []
      return [{ from: source.path, fromUnit: unitOf(source.path), specifier, target, targetUnit: unitOf(target) }]
    }),
  )
}

/** Строка нарушения для сообщения теста. */
const describeDependency = (dependency: Dependency) => `${dependency.from} → ${dependency.specifier}`

describe('изоляция модулей и слоёв', () => {
  const app = dependencies(appSources())

  it('модули не импортируют друг друга, страницы и приложение — только shared', () => {
    // when
    const violations = app.filter(
      (dep) => dep.fromUnit.startsWith('modules/') && dep.targetUnit !== dep.fromUnit && dep.targetUnit !== 'shared',
    )

    // then
    expect(violations.map(describeDependency)).toEqual([])
  })

  it('shared не зависит от модулей, страниц и приложения', () => {
    // when
    const violations = app.filter((dep) => dep.fromUnit === 'shared' && dep.targetUnit !== 'shared')

    // then
    expect(violations.map(describeDependency)).toEqual([])
  })

  it('страницы не зависят от приложения', () => {
    // when
    const violations = app.filter((dep) => dep.fromUnit === 'pages' && dep.targetUnit === 'app')

    // then
    expect(violations.map(describeDependency)).toEqual([])
  })

  it('снаружи модуль и shared доступны только через index.ts и алиас @/', () => {
    // when
    const violations = dependencies(allSources()).filter(
      (dep) =>
        dep.targetUnit !== dep.fromUnit &&
        (dep.targetUnit.startsWith('modules/') || dep.targetUnit === 'shared') &&
        dep.specifier !== `@/${dep.targetUnit}`,
    )

    // then
    expect(violations.map(describeDependency)).toEqual([])
  })

  it('относительный импорт не выходит за пределы своей единицы', () => {
    // when
    const violations = dependencies(allSources()).filter(
      (dep) => dep.specifier.startsWith('.') && dep.targetUnit !== dep.fromUnit,
    )

    // then
    expect(violations.map(describeDependency)).toEqual([])
  })
})
