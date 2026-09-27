/**
 * Генерирует типы DTO фронта из OpenAPI-контрактов backend (`../openapi/<module>-api.yaml`).
 * С флагом `--check` ничего не пишет, а падает, если сгенерированные типы устарели.
 */
import { execFileSync } from 'node:child_process'

/** Модули backend, у каждого свой контракт и свой каталог `src/modules/<module>/api`. */
const MODULES = ['identity', 'topic', 'deck', 'learning']
const check = process.argv.includes('--check')

for (const module of MODULES) {
  const args = [`../openapi/${module}-api.yaml`, '-o', `src/modules/${module}/api/schema.gen.ts`]
  execFileSync('node_modules/.bin/openapi-typescript', check ? [...args, '--check'] : args, { stdio: 'inherit' })
}
