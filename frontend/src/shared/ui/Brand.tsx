/** Логотип «памятно». */
export function Brand({ centered = false }: { centered?: boolean }) {
  return (
    <div className={centered ? 'brand brand-centered' : 'brand'}>
      <span className="brand-mark">п</span>
      <span>памятно</span>
    </div>
  )
}
