package net.pamytno.deck.service;

import java.util.UUID;

/**
 * Внутреннее событие модуля: запущено составление карточек слов по тексту.
 *
 * @param jobId       идентификатор задачи
 * @param topicId     идентификатор темы
 * @param userId      владелец темы
 * @param instruction что взять из текста; может быть пустой
 * @param text        слова, список или текст
 */
public record VocabularyGenerationRequested(UUID jobId, UUID topicId, UUID userId, String instruction,
                                            String text) {
}
