package net.pamytno.identity.exception;

import net.pamytno.common.error.ConflictException;

/**
 * Email уже занят другим пользователем.
 */
public class EmailAlreadyRegisteredException extends ConflictException {

    /** Код ошибки. */
    public static final String CODE = "EMAIL_ALREADY_REGISTERED";

    /**
     * Создаёт исключение со стандартным сообщением.
     */
    public EmailAlreadyRegisteredException() {
        super(CODE, "Пользователь с таким email уже зарегистрирован");
    }
}
