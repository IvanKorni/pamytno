package net.pamytno.common.event.topic;

import java.util.UUID;

/**
 * Пользователь создал тему. Модули запоминают владельца, чтобы принимать запросы по теме
 * ещё до того, как у неё появился текст.
 *
 * @param topicId идентификатор темы
 * @param userId  владелец темы
 */
public record TopicCreated(UUID topicId, UUID userId) {
}
