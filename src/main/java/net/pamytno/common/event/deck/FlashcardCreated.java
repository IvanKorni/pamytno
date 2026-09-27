package net.pamytno.common.event.deck;

import java.util.UUID;

/**
 * Создана карточка. Модуль обучения заводит для неё прогресс повторения.
 *
 * @param cardId  идентификатор карточки
 * @param topicId тема карточки
 * @param userId  владелец
 * @param front   вопрос на лицевой стороне
 * @param back    ответ на оборотной стороне
 */
public record FlashcardCreated(UUID cardId, UUID topicId, UUID userId, String front, String back) {
}
