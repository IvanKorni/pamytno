// Правила кода фронтенда — аналог Checkstyle backend (docs/rules/frontend.md).
// Лимиты размера не ослабляются: упёрся — дроби компонент, хук или функцию по ответственности.
import js from '@eslint/js'
import reactHooks from 'eslint-plugin-react-hooks'
import globals from 'globals'
import tseslint from 'typescript-eslint'

/** Лимиты размера кода приложения. */
const sizeLimits = {
  'max-lines': ['error', { max: 200 }],
  'max-lines-per-function': ['error', { max: 40, skipBlankLines: true, skipComments: true }],
  'max-statements': ['error', 15],
  'max-params': ['error', 5],
  'max-depth': ['error', 2],
  'max-nested-callbacks': ['error', 3],
  complexity: ['error', 8],
}

export default tseslint.config(
  { ignores: ['dist', 'node_modules', '**/*.gen.ts'] },
  {
    files: ['**/*.{ts,tsx}'],
    extends: [js.configs.recommended, ...tseslint.configs.recommended, reactHooks.configs.flat['recommended-latest']],
    languageOptions: { globals: globals.browser },
    rules: {
      ...sizeLimits,
      eqeqeq: 'error',
      'no-console': 'error',
      '@typescript-eslint/consistent-type-imports': 'error',
      '@typescript-eslint/no-explicit-any': 'error',
    },
  },
  {
    files: ['**/*.test.{ts,tsx}', 'src/test/**'],
    rules: { 'max-lines': ['error', { max: 250 }], 'max-lines-per-function': 'off', 'max-statements': 'off' },
  },
  {
    files: ['*.{js,mjs,ts}', 'scripts/**'],
    extends: [js.configs.recommended],
    languageOptions: { globals: globals.node },
  },
)
