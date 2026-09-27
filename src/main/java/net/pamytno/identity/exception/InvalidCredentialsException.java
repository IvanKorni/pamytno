package net.pamytno.identity.exception;

import net.pamytno.common.error.UnauthorizedException;

/**
 * Неверный email или пароль. Намеренно не уточняет, что именно неверно.
 */
public class InvalidCredentialsException extends UnauthorizedException {

    /** Код ошибки. */
    public static final String CODE = "INVALID_CREDENTIALS";

    /**
     * Создаёт исключение со стандартным сообщением.
     */
    public InvalidCredentialsException() {
        super(CODE, "Неверный email или пароль");
    }
}
