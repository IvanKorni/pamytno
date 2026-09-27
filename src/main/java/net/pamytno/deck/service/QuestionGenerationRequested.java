package net.pamytno.deck.service;

import java.util.UUID;

/**
 * Внутреннее событие модуля: запущена задача генерации вопросов.
 *
 * @param jobId   идентификатор задачи
 * @param topicId идентификатор темы
 * @param userId  владелец темы
 */
public record QuestionGenerationRequested(UUID jobId, UUID topicId, UUID userId) {
}
