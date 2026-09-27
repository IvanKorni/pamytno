/**
 * Создаёт на локальном backend демо-пользователя с данными для ручной проверки фронта:
 * тема с готовыми карточками к повторению и отобранными вопросами, тема с вопросами на отбор
 * и пустая тема для добавления материалов. Повторный запуск ничего не дублирует.
 *
 * Запуск: `npm run seed:demo` (backend — `PAMYTNO_BACKEND`, по умолчанию http://localhost:8080).
 * Учётные данные — только для локальной разработки.
 */

/** Адрес backend. */
const BACKEND = process.env.PAMYTNO_BACKEND ?? 'http://localhost:8080'

/** Демо-пользователь для ручной проверки. */
const DEMO = { email: 'demo@pamytno.dev', password: 'pamytno-demo-2026' }

/** Материал первой темы: из него получатся карточки к повторению. */
const TRANSACTIONS =
  'MVCC в PostgreSQL позволяет читателям не блокировать писателей. Каждая строка хранит xmin и xmax. ' +
  'Read Committed видит данные, зафиксированные до начала каждого запроса. ' +
  'Repeatable Read видит снимок на начало транзакции. ' +
  'Serializable обнаруживает аномалии сериализации и откатывает одну из транзакций. ' +
  'Оптимистичная блокировка проверяет версию строки при обновлении. ' +
  'Пессимистичная блокировка берёт SELECT FOR UPDATE.'

/** Материал второй темы: вопросы по нему остаются на отбор. */
const SECURITY =
  'Spring Security строит цепочку фильтров SecurityFilterChain. ' +
  'JWT передаётся в заголовке Authorization со схемой Bearer. ' +
  'BCrypt хранит пароль с солью и настраиваемой стоимостью. ' +
  'CSRF-защиту отключают для stateless API без cookie.'

let token

/** Запрос к API с токеном демо-пользователя; ошибка backend прерывает сид. */
async function api(method, path, body) {
  const headers = { 'Content-Type': 'application/json', ...(token && { Authorization: `Bearer ${token}` }) }
  const response = await fetch(`${BACKEND}/api${path}`, { method, headers, body: body && JSON.stringify(body) })
  const payload = response.status === 204 ? undefined : await response.json()
  if (!response.ok)
    throw Object.assign(new Error(`${method} ${path}: ${payload?.message}`), { status: response.status })
  return payload
}

/** Ждёт, пока значение не станет готовым. */
async function eventually(load, ready) {
  for (let attempt = 0; attempt < 60; attempt++) {
    const value = await load()
    if (ready(value)) return value
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
  throw new Error('backend не закончил обработку за 30 секунд')
}

/** Регистрирует демо-пользователя или входит, если он уже есть. */
async function signIn() {
  const auth = await api('POST', '/auth/register', DEMO).catch((error) => {
    if (error.status !== 409) throw error
    return api('POST', '/auth/login', DEMO)
  })
  token = auth.accessToken
}

/** Создаёт тему с текстом и ждёт, пока текст обработается. */
async function topicWithText(title, description, text) {
  const topic = await api('POST', '/topics', { title, description })
  await api('POST', `/topics/${topic.id}/sources/text`, { type: 'TEXT', name: 'Конспект', text })
  await eventually(
    () => api('GET', `/topics/${topic.id}/sources`),
    (sources) => sources.every((source) => source.status === 'READY'),
  )
  return topic
}

/** Запускает генерацию, когда deck получил текст темы (он строит его копию асинхронно), и ждёт её завершения. */
async function generate(path) {
  const job = await eventually(
    () => api('POST', path).catch((error) => (error.status === 409 ? undefined : Promise.reject(error))),
    Boolean,
  )
  await eventually(
    () => api('GET', `/generation-jobs/${job.id}`),
    (current) => current.status !== 'PROCESSING',
  )
}

/** Тема с карточками к повторению: вопросы сгенерированы, большая часть отобрана, карточки созданы. */
async function seedTransactions() {
  const topic = await topicWithText('Транзакции PostgreSQL', 'Изоляция, MVCC и блокировки', TRANSACTIONS)
  await generate(`/topics/${topic.id}/questions/generate`)
  const questions = await api('GET', `/topics/${topic.id}/questions`)
  const [rejected, ...approved] = questions
  await api('POST', '/questions/decisions', { questionIds: approved.map((q) => q.id), decision: 'APPROVE' })
  await api('POST', '/questions/decisions', { questionIds: [rejected.id], decision: 'REJECT' })
  await generate(`/topics/${topic.id}/cards/generate`)
  return approved.length
}

/** Тема с вопросами на отбор: карточек пока нет. */
async function seedSecurity() {
  const topic = await topicWithText('Spring Security', 'Фильтры, JWT и хранение паролей', SECURITY)
  await generate(`/topics/${topic.id}/questions/generate`)
}

await signIn()
const existing = await api('GET', '/topics')
if (existing.length) {
  console.log(`Демо-пользователь уже заполнен: ${existing.length} темы.`)
} else {
  const cards = await seedTransactions()
  await seedSecurity()
  await api('POST', '/topics', { title: 'Английские слова', description: 'Пустая тема — добавьте список слов или PDF' })
  console.log(`Готово: 3 темы, ${cards} карточек к повторению.`)
}
console.log(`Вход: ${DEMO.email} / ${DEMO.password} → http://localhost:5173`)
