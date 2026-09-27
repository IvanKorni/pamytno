/** Загрузка целого экрана. */
export function PageLoading({ text = 'Загружаем…' }: { text?: string }) {
  return (
    <div className="page-loader">
      <div className="spinner" /> {text}
    </div>
  )
}

/** Загрузка блока внутри экрана. */
export function InlineLoading() {
  return (
    <div className="inline-loading">
      <div className="spinner small-spinner" /> Загружаем…
    </div>
  )
}
