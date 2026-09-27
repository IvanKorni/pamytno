package net.pamytno.learning.domain;

import java.util.UUID;

/**
 * Ссылка на карточку модуля {@code deck} с её темой и владельцем.
 *
 * @param cardId  идентификатор карточки
 * @param topicId тема карточки
 * @param userId  владелец
 */
public record CardRef(UUID cardId, UUID topicId, UUID userId) {
}
