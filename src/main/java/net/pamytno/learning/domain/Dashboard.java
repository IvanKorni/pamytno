package net.pamytno.learning.domain;

import java.util.List;

/**
 * Общий прогресс пользователя.
 *
 * @param totals итоги по всем темам
 * @param topics прогресс каждой темы с карточками
 */
public record Dashboard(ProgressStats totals, List<TopicProgress> topics) {
}
