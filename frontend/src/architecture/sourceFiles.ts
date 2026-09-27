import { readdirSync, readFileSync } from 'node:fs'
import { dirname, join, normalize, relative } from 'node:path'
import { fileURLToPath } from 'node:url'
import ts from 'typescript'

/** Каталог `src` фронтенда. */
const SRC = fileURLToPath(new URL('..', import.meta.url))

/** Исходный файл: путь от `src` и разобранное дерево TypeScript. */
export interface Source {
  path: string
  ast: ts.SourceFile
}

/** Все `.ts` / `.tsx` файлы `src`, кроме сгенерированных из OpenAPI. */
export function allSources(): Source[] {
  return listFiles(SRC)
    .filter((file) => /\.tsx?$/.test(file) && !file.endsWith('.gen.ts') && !file.endsWith('.d.ts'))
    .map((file) => parse(relative(SRC, file)))
}

/** Код приложения — без тестов, тестовой инфраструктуры и архитектурных проверок. */
export function appSources(): Source[] {
  return allSources().filter((source) => !isTestCode(source.path))
}

/** Файлы тестов. */
export function testSources(): Source[] {
  return allSources().filter((source) => /\.test\.tsx?$/.test(source.path))
}

/** Код, относящийся к тестам: сами тесты, `src/test` и `src/architecture`. */
export function isTestCode(path: string): boolean {
  return /\.test\.tsx?$/.test(path) || path.startsWith('test/') || path.startsWith('architecture/')
}

/** Спецификаторы импортов и реэкспортов файла. */
export function importsOf(source: Source): string[] {
  return source.ast.statements
    .filter((node) => ts.isImportDeclaration(node) || ts.isExportDeclaration(node))
    .map((node) => node.moduleSpecifier)
    .filter((specifier): specifier is ts.StringLiteral => Boolean(specifier && ts.isStringLiteral(specifier)))
    .map((specifier) => specifier.text)
}

/** Путь от `src`, в который ведёт импорт, или `undefined` для внешнего пакета. */
export function resolveImport(source: Source, specifier: string): string | undefined {
  if (specifier.startsWith('@/')) return normalize(specifier.slice(2))
  if (specifier.startsWith('.')) return normalize(join(dirname(source.path), specifier))
  return undefined
}

/** Слои и служебные каталоги `src`; файлы в корне `src` (точка входа) относятся к `app`. */
const UNITS = ['app', 'pages', 'shared', 'test', 'architecture']

/** Единица архитектуры, к которой относится путь: слой, `modules/<module>` или служебный каталог. */
export function unitOf(path: string): string {
  const [first, second] = path.split('/')
  if (first === 'modules') return `modules/${second}`
  return UNITS.includes(first) ? first : 'app'
}

/** Обходит все узлы дерева. */
export function walk(node: ts.Node, visit: (node: ts.Node) => void): void {
  visit(node)
  node.forEachChild((child) => walk(child, visit))
}

/** Разбирает файл по пути от `src`. */
function parse(path: string): Source {
  const text = readFileSync(join(SRC, path), 'utf8')
  return { path, ast: ts.createSourceFile(path, text, ts.ScriptTarget.Latest, true) }
}

/** Все файлы каталога рекурсивно. */
function listFiles(dir: string): string[] {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name)
    return entry.isDirectory() ? listFiles(path) : [path]
  })
}
