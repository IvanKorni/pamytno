import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { saveMaterial, type MaterialDraft } from '../model/materialDraft'
import { sourcesPollInterval } from '../model/sourcePolling'
import { deleteSource, getTopicContent, listSources } from './sourceApi'
import { topicKeys } from './topicKeys'

/** Источники темы; опрашиваются, пока backend их обрабатывает. */
export function useSources(topicId: string) {
  return useQuery({
    queryKey: topicKeys.sources(topicId),
    queryFn: () => listSources(topicId),
    refetchInterval: (query) => sourcesPollInterval(query.state.data),
  })
}

/** Единый текст темы; загружается, только когда его открыли. */
export function useTopicContent(topicId: string, enabled: boolean) {
  return useQuery({ queryKey: topicKeys.content(topicId), queryFn: () => getTopicContent(topicId), enabled })
}

/** Добавление материала любого типа. */
export function useAddMaterial(topicId: string, onAdded: () => void) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (draft: MaterialDraft) => saveMaterial(topicId, draft),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.sources(topicId) })
      queryClient.invalidateQueries({ queryKey: topicKeys.detail(topicId) })
      onAdded()
    },
  })
}

/** Удаление источника. */
export function useDeleteSource(topicId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteSource,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: topicKeys.sources(topicId) }),
  })
}
