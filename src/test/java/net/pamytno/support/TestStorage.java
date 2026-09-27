package net.pamytno.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Временный каталог файлового хранилища, общий для всех тестовых контекстов.
 */
public final class TestStorage {

    private static final Path ROOT = createRoot();

    /**
     * Запрещает создание экземпляров.
     */
    private TestStorage() {
    }

    /**
     * Корень хранилища для тестов.
     *
     * @return путь к временному каталогу
     */
    public static Path root() {
        return ROOT;
    }

    /**
     * Создаёт временный каталог.
     *
     * @return путь к каталогу
     */
    private static Path createRoot() {
        try {
            return Files.createTempDirectory("pamytno-storage-");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
