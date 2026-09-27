import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { Topic } from '../model/types'
import { createTopic, deleteTopic, getTopic, listTopics, updateTopic, type TopicChanges } from './topicApi'
import { topicKeys } from './topicKeys'

/** Темы текущего пользователя. */
export function useTopics() {
  return useQuery({ queryKey: topicKeys.all, queryFn: listTopics })
}

/** Тема по идентификатору. */
export function useTopic(topicId: string) {
  return useQuery({ queryKey: topicKeys.detail(topicId), queryFn: () => getTopic(topicId) })
}

/** Создание темы; после успеха список тем перечитывается. */
export function useCreateTopic(onCreated: (topic: Topic) => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ title, description }: { title: string; description: string }) => createTopic(title, description),
    onSuccess: async (topic) => {
      await queryClient.invalidateQueries({ queryKey: topicKeys.all })
      onCreated(topic)
    },
  })
}

/** Изменение названия и описания темы. */
export function useUpdateTopic(topicId: string, onUpdated: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (changes: TopicChanges) => updateTopic(topicId, changes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.detail(topicId) })
      queryClient.invalidateQueries({ queryKey: topicKeys.all })
      onUpdated()
    },
  })
}

/** Удаление темы; после успеха список тем перечитывается. */
export function useDeleteTopic(topicId: string, onDeleted: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => deleteTopic(topicId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.all })
      onDeleted()
    },
  })
}
