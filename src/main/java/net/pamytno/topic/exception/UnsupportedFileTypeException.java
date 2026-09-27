package net.pamytno.topic.exception;

import net.pamytno.common.error.BadRequestException;

/**
 * Загруженный файл не является PDF.
 */
public class UnsupportedFileTypeException extends BadRequestException {

    /** Код ошибки. */
    public static final String CODE = "UNSUPPORTED_FILE_TYPE";

    /**
     * Создаёт исключение со стандартным сообщением.
     */
    public UnsupportedFileTypeException() {
        super(CODE, "Поддерживаются только PDF-файлы");
    }
}
