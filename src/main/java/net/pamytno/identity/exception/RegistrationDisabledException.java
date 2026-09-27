package net.pamytno.identity.exception;

import net.pamytno.common.error.ForbiddenException;

/**
 * Регистрация выключена настройкой {@code pamytno.identity.registration-enabled}.
 */
public class RegistrationDisabledException extends ForbiddenException {

    /** Код ошибки. */
    public static final String CODE = "REGISTRATION_DISABLED";

    /**
     * Создаёт исключение со стандартным сообщением.
     */
    public RegistrationDisabledException() {
        super(CODE, "Регистрация новых пользователей отключена");
    }
}
