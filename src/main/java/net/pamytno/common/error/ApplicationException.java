package net.pamytno.common.error;

import lombok.Getter;

/**
 * Базовое исключение приложения со стабильным кодом ошибки.
 * Модули наследуют не его, а категории: {@link NotFoundException}, {@link ConflictException},
 * {@link BadRequestException}, {@link UnauthorizedException}, {@link ForbiddenException}.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    private final String code;

    /**
     * Создаёт исключение с кодом и сообщением.
     *
     * @param code    стабильный код ошибки
     * @param message сообщение на русском
     */
    protected ApplicationException(String code, String message) {
        super(message);
        this.code = code;
    }
}
