package net.pamytno.topic.domain;

/**
 * Вид источника материала.
 */
public enum SourceType {

    /** PDF-файл, текст извлекается PDFBox. */
    PDF,
    /** Видео YouTube, текст — субтитры. */
    YOUTUBE,
    /** Произвольный текст. */
    TEXT,
    /** Список слов, пар «слово — перевод» или строк. */
    WORD_LIST;

    /**
     * Проверяет, что источник этого вида передаётся текстом напрямую.
     *
     * @return {@code true} для {@link #TEXT} и {@link #WORD_LIST}
     */
    public boolean isTextual() {
        return this == TEXT || this == WORD_LIST;
    }
}
