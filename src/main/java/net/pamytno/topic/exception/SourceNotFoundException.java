package net.pamytno.topic.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Источник не найден или принадлежит другому пользователю.
 */
public class SourceNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "SOURCE_NOT_FOUND";

    /**
     * Создаёт исключение для источника.
     *
     * @param sourceId идентификатор источника
     */
    public SourceNotFoundException(UUID sourceId) {
        super(CODE, "Источник " + sourceId + " не найден");
    }
}
