package net.pamytno.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Javadoc пишется на русском: каждый блок {@code /** ... *}{@code /} содержит кириллицу.
 */
class JavadocLanguageTest {

    private static final Pattern JAVADOC = Pattern.compile("/\\*\\*(.*?)\\*/", Pattern.DOTALL);
    private static final Pattern CYRILLIC = Pattern.compile("[А-Яа-яЁё]");
    private static final List<Path> SOURCE_ROOTS = List.of(Path.of("src/main/java"), Path.of("src/test/java"));

    @Test
    @DisplayName("Каждый Javadoc-комментарий написан на русском")
    void javadoc_isWrittenInRussian() {
        var violations = SOURCE_ROOTS.stream()
                .flatMap(JavadocLanguageTest::javaFiles)
                .flatMap(JavadocLanguageTest::nonRussianJavadocs)
                .toList();

        assertThat(violations).as("Javadoc без кириллицы").isEmpty();
    }

    /**
     * Перечисляет Java-файлы в каталоге исходников.
     *
     * @param root корень исходников
     * @return поток путей к .java файлам
     */
    private static Stream<Path> javaFiles(Path root) {
        try (var files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList().stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Находит в файле Javadoc-блоки без кириллицы.
     *
     * @param file Java-файл
     * @return описания нарушений вида {@code "Файл.java:12"}
     */
    private static Stream<String> nonRussianJavadocs(Path file) {
        var source = read(file);
        Matcher matcher = JAVADOC.matcher(source);
        return matcher.results()
                .filter(match -> !CYRILLIC.matcher(match.group(1)).find())
                .map(match -> file + ":" + lineOf(source, match.start()));
    }

    /**
     * Читает файл целиком.
     *
     * @param file путь к файлу
     * @return содержимое файла
     */
    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Вычисляет номер строки по смещению в тексте.
     *
     * @param source текст файла
     * @param offset смещение символа
     * @return номер строки, начиная с 1
     */
    private static long lineOf(String source, int offset) {
        return source.substring(0, offset).chars().filter(ch -> ch == '\n').count() + 1;
    }
}
