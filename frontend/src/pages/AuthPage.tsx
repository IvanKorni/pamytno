import { useState, type FormEvent } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useSignIn } from '@/modules/identity'
import { Brand, InlineError } from '@/shared'

/** Тексты экрана входа и регистрации. */
const TEXTS = {
  login: {
    title: 'С возвращением',
    lead: 'Войдите, чтобы продолжить изучение.',
    submit: 'Войти',
    switchText: 'Впервые здесь?',
    switchLink: 'Создать аккаунт',
    switchTo: '/register',
  },
  register: {
    title: 'Создайте свой ритм обучения',
    lead: 'Собирайте материалы и превращайте их в карточки.',
    submit: 'Зарегистрироваться',
    switchText: 'Уже есть аккаунт?',
    switchLink: 'Войти',
    switchTo: '/login',
  },
}

/** Что делает форма: входит или регистрирует. */
type Mode = 'login' | 'register'

/**
 * Экран входа или регистрации по email и паролю. После входа маршрутизатор сам вернёт пользователя
 * на исходный адрес; ссылка на соседний экран этот адрес сохраняет.
 */
export function AuthPage({ mode }: { mode: Mode }) {
  const location = useLocation()
  const texts = TEXTS[mode]
  return (
    <div className="auth-page">
      <div className="auth-card">
        <Brand />
        <h1>{texts.title}</h1>
        <p className="auth-lead">{texts.lead}</p>
        <CredentialsForm key={mode} mode={mode} submitLabel={texts.submit} />
        <p className="auth-switch">
          {texts.switchText}{' '}
          <Link to={texts.switchTo} state={location.state}>
            {texts.switchLink}
          </Link>
        </p>
      </div>
    </div>
  )
}

/** Форма email и пароля; пароль не короче 8 символов требуется только при регистрации. */
function CredentialsForm({ mode, submitLabel }: { mode: Mode; submitLabel: string }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const signIn = useSignIn(mode)
  const submit = (event: FormEvent) => {
    event.preventDefault()
    signIn.mutate({ email, password })
  }
  return (
    <form onSubmit={submit} className="form-stack">
      {signIn.error && <InlineError message={signIn.error.message} />}
      <label>
        Электронная почта
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          maxLength={255}
          required
          autoFocus
        />
      </label>
      <label>
        Пароль
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={mode === 'register' ? 8 : undefined}
          maxLength={72}
          required
        />
      </label>
      <button className="button button-primary button-wide" disabled={signIn.isPending}>
        {signIn.isPending ? 'Подождите…' : submitLabel}
      </button>
    </form>
  )
}
