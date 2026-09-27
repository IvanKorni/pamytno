import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useToast } from '@/shared'
import { saveMaterial, type MaterialDraft } from '../model/materialDraft'
import { sourcesPollInterval } from '../model/sourcePolling'
import type { Source } from '../model/types'
import { deleteSource, getTopicContent, listSources } from './sourceApi'
import { topicKeys } from './topicKeys'

/**
 * Источники темы; опрашиваются, пока backend их обрабатывает. Когда обработка закончилась,
 * перечитываются статус темы и список тем — они зависят от готовности источников.
 */
export function useSources(topicId: string) {
  const queryClient = useQueryClient()
  const refresh = useTopicMaterialsRefresh(topicId)
  return useQuery({
    queryKey: topicKeys.sources(topicId),
    queryFn: async () => {
      const before = queryClient.getQueryData<Source[]>(topicKeys.sources(topicId))
      const sources = await listSources(topicId)
      if (sourcesPollInterval(before) && !sourcesPollInterval(sources)) refresh({ keepSources: true })
      return sources
    },
    refetchInterval: (query) => sourcesPollInterval(query.state.data),
  })
}

/** Единый текст темы; загружается заново при каждом открытии — он пересобирается при изменении источников. */
export function useTopicContent(topicId: string, enabled: boolean) {
  return useQuery({
    queryKey: topicKeys.content(topicId),
    queryFn: () => getTopicContent(topicId),
    enabled,
    staleTime: 0,
  })
}

/** Добавление материала любого типа. */
export function useAddMaterial(topicId: string, onAdded: () => void) {
  const refresh = useTopicMaterialsRefresh(topicId)
  return useMutation({
    mutationFn: (draft: MaterialDraft) => saveMaterial(topicId, draft),
    onSuccess: () => {
      refresh()
      onAdded()
    },
  })
}

/** Удаление источника; ошибка показывается уведомлением. */
export function useDeleteSource(topicId: string) {
  const refresh = useTopicMaterialsRefresh(topicId)
  const showToast = useToast((toast) => toast.show)
  return useMutation({
    mutationFn: deleteSource,
    onSuccess: () => refresh(),
    onError: (error) => showToast(`Не удалось удалить материал: ${error.message}`, 'error'),
  })
}

/** Перечитывает всё, что зависит от набора источников: сами источники, единый текст и статус темы. */
function useTopicMaterialsRefresh(topicId: string) {
  const queryClient = useQueryClient()
  return ({ keepSources = false } = {}) => {
    if (!keepSources) queryClient.invalidateQueries({ queryKey: topicKeys.sources(topicId) })
    queryClient.invalidateQueries({ queryKey: topicKeys.content(topicId) })
    queryClient.invalidateQueries({ queryKey: topicKeys.detail(topicId) })
    queryClient.invalidateQueries({ queryKey: topicKeys.all })
  }
}
