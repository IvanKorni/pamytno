/** Загрузка целого экрана. */
export function PageLoading({ text = 'Загружаем…' }: { text?: string }) {
  return (
    <div className="page-loader" role="status">
      <div className="spinner" /> {text}
    </div>
  )
}

/** Загрузка блока внутри экрана. */
export function InlineLoading() {
  return (
    <div className="inline-loading" role="status">
      <div className="spinner" /> Загружаем…
    </div>
  )
}
