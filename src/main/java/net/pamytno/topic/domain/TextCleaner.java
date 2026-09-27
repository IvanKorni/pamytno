package net.pamytno.topic.domain;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Техническая очистка извлечённого текста. Смысл и содержание не меняются:
 * убираются служебные символы, пустые строки и лишние пробелы.
 */
public final class TextCleaner {

    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n?");
    private static final Pattern INVISIBLE =
            Pattern.compile("[\\p{Cc}&&[^\\n\\t]]|[\\u00AD\\u200B-\\u200D\\u2060\\uFEFF]");
    private static final Pattern HORIZONTAL_SPACES = Pattern.compile("\\h+");
    private static final Pattern HYPHENATED_LINE_BREAK = Pattern.compile("(\\p{L})-\\h*\\R\\h*(\\p{Ll})");

    /**
     * Запрещает создание экземпляров.
     */
    private TextCleaner() {
    }

    /**
     * Очищает текст.
     *
     * @param raw исходный текст
     * @return текст без служебных символов, пустых строк и повторяющихся пробелов
     */
    public static String clean(String raw) {
        var text = LINE_BREAK.matcher(raw).replaceAll("\n");
        text = INVISIBLE.matcher(text).replaceAll("");
        return text.lines()
                .map(line -> HORIZONTAL_SPACES.matcher(line).replaceAll(" ").strip())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Склеивает слова, разорванные переносом в конце строки ({@code "обра-\nботка"} → {@code "обработка"}).
     * Типичная проблема извлечения текста из PDF, поэтому применяется только к PDF.
     *
     * @param text извлечённый текст
     * @return текст со склеенными переносами
     */
    public static String joinHyphenatedLineBreaks(String text) {
        return HYPHENATED_LINE_BREAK.matcher(text).replaceAll("$1$2");
    }
}
