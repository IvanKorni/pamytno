package net.pamytno.learning.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Карточки нет в повторении: она не найдена, чужая или ещё не обработана после создания.
 */
public class CardProgressNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "CARD_PROGRESS_NOT_FOUND";

    /**
     * Создаёт исключение.
     *
     * @param id идентификатор карточки
     */
    public CardProgressNotFoundException(UUID id) {
        super(CODE, "Карточка " + id + " не найдена в повторении");
    }
}
