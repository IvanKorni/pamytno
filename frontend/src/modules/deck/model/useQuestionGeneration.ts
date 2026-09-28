import { useQueryClient } from '@tanstack/react-query'
import { generateQuestions } from '../api/deckApi'
import { deckKeys } from '../api/deckKeys'
import { useGenerationRun } from './useGenerationRun'

/**
 * Генерация вопросов темы: запуск и опрос задачи, когда вопросы готовы — список вопросов перечитывается.
 * Запущенная задача запоминается, поэтому её можно начать на одном экране, а следить за ней на другом.
 */
export function useQuestionGeneration(topicId: string) {
  const queryClient = useQueryClient()
  return useGenerationRun({
    topicId,
    kind: 'QUESTIONS',
    start: () => generateQuestions(topicId),
    onReady: () => queryClient.invalidateQueries({ queryKey: deckKeys.questions(topicId) }),
    failureText: 'Не удалось создать вопросы.',
  })
}
