package net.pamytno.learning.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Учебная сессия не найдена или принадлежит другому пользователю.
 */
public class LearningSessionNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "LEARNING_SESSION_NOT_FOUND";

    /**
     * Создаёт исключение.
     *
     * @param id идентификатор сессии
     */
    public LearningSessionNotFoundException(UUID id) {
        super(CODE, "Учебная сессия " + id + " не найдена");
    }
}
