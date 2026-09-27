package net.pamytno.common.error;

import java.time.Instant;

/**
 * Единый формат ошибки REST API.
 *
 * @param code      стабильный машиночитаемый код ошибки, например {@code TOPIC_NOT_FOUND}
 * @param message   человекочитаемое описание на русском
 * @param timestamp момент возникновения ошибки
 */
public record ErrorResponse(String code, String message, Instant timestamp) {
}
