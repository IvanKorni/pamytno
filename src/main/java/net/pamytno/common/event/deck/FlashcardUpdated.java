package net.pamytno.common.event.deck;

import java.util.UUID;

/**
 * Пользователь изменил текст карточки. Модуль обучения обновляет свою копию.
 *
 * @param cardId  идентификатор карточки
 * @param topicId тема карточки
 * @param userId  владелец
 * @param front   новый вопрос
 * @param back    новый ответ
 */
public record FlashcardUpdated(UUID cardId, UUID topicId, UUID userId, String front, String back) {
}
