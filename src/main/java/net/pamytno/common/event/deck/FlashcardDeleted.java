package net.pamytno.common.event.deck;

import java.util.UUID;

/**
 * Карточка удалена. Модуль обучения удаляет её прогресс.
 *
 * @param cardId  идентификатор карточки
 * @param topicId тема карточки
 * @param userId  владелец
 */
public record FlashcardDeleted(UUID cardId, UUID topicId, UUID userId) {
}
