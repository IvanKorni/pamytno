/** Строка с ошибкой внутри формы или блока. */
export function InlineError({ message }: { message: string }) {
  return <div className="inline-error">{message}</div>
}
