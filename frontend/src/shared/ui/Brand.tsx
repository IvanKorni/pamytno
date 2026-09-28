/** Название приложения, которое показывается в шапке и на экране входа. */
export const APP_NAME = 'Напоминатор'

/** Логотип «Напоминатор» — название без значка, как в макете. */
export function Brand() {
  return <span className="brand">{APP_NAME}</span>
}
