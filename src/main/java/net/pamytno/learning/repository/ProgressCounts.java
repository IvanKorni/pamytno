package net.pamytno.learning.repository;

import java.util.UUID;

/**
 * Счётчики прогресса темы, посчитанные одним SQL-запросом.
 *
 * @param topicId  тема
 * @param total    всего карточек
 * @param fresh    ни разу не повторялись
 * @param mastered изучены
 * @param due      к повторению сейчас
 * @param dueToday к повторению до конца дня
 */
public record ProgressCounts(UUID topicId, Long total, Long fresh, Long mastered, Long due, Long dueToday) {
}
