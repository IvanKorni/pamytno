import { useEffect, useState } from 'react'

/** Сколько пикселей прокрутить, чтобы шапка получила фон и границу. */
const SCROLL_THRESHOLD_PX = 40

/** Прокручена ли страница: пока нет, шапка прозрачна и сливается с экраном. */
export function useScrolled(): boolean {
  const [scrolled, setScrolled] = useState(false)
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > SCROLL_THRESHOLD_PX)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])
  return scrolled
}
