package net.pamytno.common.event.topic;

import java.util.UUID;

/**
 * Тема удалена вместе с материалами. Модули удаляют всё, что построили по этой теме.
 *
 * @param topicId идентификатор удалённой темы
 * @param userId  владелец темы
 */
public record TopicDeleted(UUID topicId, UUID userId) {
}
