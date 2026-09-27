package net.pamytno.topic.service;

import java.util.UUID;

/**
 * Внутреннее событие модуля: источник сохранён и ждёт асинхронной обработки.
 *
 * @param sourceId идентификатор источника
 */
public record SourceSubmitted(UUID sourceId) {
}
