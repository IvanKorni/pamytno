import { useParams } from 'react-router-dom'

/** Идентификатор темы из адреса `/topics/:topicId/...`; экраны темы открываются только по такому адресу. */
export function useTopicId(): string {
  const { topicId } = useParams()
  if (!topicId) throw new Error('В адресе нет идентификатора темы')
  return topicId
}
