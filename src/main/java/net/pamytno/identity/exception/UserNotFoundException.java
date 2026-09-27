package net.pamytno.identity.exception;

import net.pamytno.common.error.NotFoundException;

import java.util.UUID;

/**
 * Пользователь из токена не найден (например, удалён).
 */
public class UserNotFoundException extends NotFoundException {

    /** Код ошибки. */
    public static final String CODE = "USER_NOT_FOUND";

    /**
     * Создаёт исключение для идентификатора пользователя.
     *
     * @param userId идентификатор пользователя
     */
    public UserNotFoundException(UUID userId) {
        super(CODE, "Пользователь " + userId + " не найден");
    }
}
