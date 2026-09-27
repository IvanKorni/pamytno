package net.pamytno.deck.domain;

import java.util.UUID;

/**
 * Ссылка на тему из модуля {@code topic} с её владельцем. Модуль {@code deck} знает о теме
 * только это — из интеграционных событий.
 *
 * @param topicId идентификатор темы
 * @param userId  владелец темы
 */
public record TopicRef(UUID topicId, UUID userId) {
}
