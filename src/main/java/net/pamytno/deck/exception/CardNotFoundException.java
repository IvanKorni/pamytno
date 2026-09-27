package net.pamytno.deck.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Карточка не найдена или принадлежит другому пользователю.
 */
public class CardNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "CARD_NOT_FOUND";

    /**
     * Создаёт исключение для карточки.
     *
     * @param cardId идентификатор карточки
     */
    public CardNotFoundException(UUID cardId) {
        super(CODE, "Карточка " + cardId + " не найдена");
    }
}
