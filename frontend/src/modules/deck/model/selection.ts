import { useState } from 'react'
import type { Question } from './types'

/**
 * Оставляет в выборе только новые вопросы: вопрос, по которому уже решили по одному
 * или из которого сделали карточку, из выбора выпадает — иначе «Создать карточки» одобрил бы
 * вопрос, который пользователь отклонил.
 */
export function pendingSelection(selected: string[], questions: Question[] = []): string[] {
  const pending = new Set(questions.filter((item) => item.status === 'GENERATED').map((item) => item.id))
  return selected.filter((id) => pending.has(id))
}

/** Выбор вопросов в списке; наружу отдаются только ещё новые вопросы. */
export function useQuestionSelection(questions?: Question[]): [string[], (ids: string[]) => void] {
  const [checked, setChecked] = useState<string[]>([])
  return [pendingSelection(checked, questions), setChecked]
}
