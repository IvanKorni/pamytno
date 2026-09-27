import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useSignIn } from '@/modules/identity'
import { InlineError } from '@/shared'

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

/** Экран входа или регистрации по email и паролю. */
export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const signIn = useSignIn(mode, () => navigate('/'))
  const texts = TEXTS[mode]
  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="brand brand-centered">
          <span className="brand-mark">п</span>
          <span>памятно</span>
        </div>
        <div className="eyebrow">Личные знания, без шума</div>
        <h1>{texts.title}</h1>
        <p className="muted">{texts.lead}</p>
        {signIn.error && <InlineError message={signIn.error.message} />}
        <form
          onSubmit={(e) => {
            e.preventDefault()
            signIn.mutate({ email, password })
          }}
          className="form-stack"
        >
          <label>
            Электронная почта
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
          </label>
          <label>
            Пароль
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              required
            />
          </label>
          <button className="button button-primary button-wide" disabled={signIn.isPending}>
            {signIn.isPending ? 'Подождите…' : texts.submit}
          </button>
        </form>
        <p className="auth-switch">
          {texts.switchText} <Link to={texts.switchTo}>{texts.switchLink}</Link>
        </p>
      </div>
    </div>
  )
}
