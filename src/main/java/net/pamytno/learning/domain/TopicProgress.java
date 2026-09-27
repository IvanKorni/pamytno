package net.pamytno.learning.domain;

import java.util.UUID;

/**
 * Прогресс изучения темы.
 *
 * @param topicId идентификатор темы
 * @param stats   показатели
 */
public record TopicProgress(UUID topicId, ProgressStats stats) {
}
