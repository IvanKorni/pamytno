package net.pamytno.deck.domain;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Карточка английского слова или выражения. На лицевой стороне — объяснение на простом английском и
 * предложение с пропуском, на оборотной — само выражение, русский перевод и перевод предложения.
 *
 * @param word               выражение в словарной форме
 * @param translation        русский перевод выражения
 * @param definition         объяснение значения на простом английском
 * @param example            предложение с пропуском {@value #GAP}
 * @param exampleTranslation перевод предложения на русский
 */
public record WordCard(String word, String translation, String definition, String example,
                       String exampleTranslation) {

    /** Пропуск на месте выражения в предложении. */
    public static final String GAP = "_____";

    private static final Pattern ANY_GAP = Pattern.compile("_{3,}");

    /**
     * Проверяет ответ модели и приводит пропуск к одному виду. Если модель вписала выражение вместо
     * пропуска, выражение заменяется пропуском.
     *
     * @param word               выражение
     * @param translation        перевод выражения
     * @param definition         объяснение значения
     * @param example            предложение с пропуском или с самим выражением
     * @param exampleTranslation перевод предложения
     * @return карточка или пустое значение, если какого-то поля нет или пропуск не получился
     */
    public static Optional<WordCard> of(String word, String translation, String definition, String example,
                                        String exampleTranslation) {
        if (isAnyBlank(word, translation, definition, example, exampleTranslation)) {
            return Optional.empty();
        }
        return withGap(example.strip(), word.strip()).map(gapped -> new WordCard(word.strip(), translation.strip(),
                definition.strip(), gapped, exampleTranslation.strip()));
    }

    /**
     * Лицевая сторона: объяснение и предложение с пропуском отдельными абзацами.
     *
     * @return текст лицевой стороны
     */
    public String front() {
        return definition + "\n\n" + example;
    }

    /**
     * Оборотная сторона: выражение жирным, под ним перевод, отдельным абзацем — перевод предложения.
     *
     * @return текст оборотной стороны
     */
    public String back() {
        return "**" + word + "**\n" + translation + "\n\n" + exampleTranslation;
    }

    /**
     * Предложение с единым видом пропуска.
     *
     * @param example предложение от модели
     * @param word    выражение
     * @return предложение с пропуском или пустое значение, если пропуска нет и выражение в предложении не найдено
     */
    private static Optional<String> withGap(String example, String word) {
        if (ANY_GAP.matcher(example).find()) {
            return Optional.of(ANY_GAP.matcher(example).replaceAll(GAP));
        }
        var start = example.toLowerCase(Locale.ROOT).indexOf(word.toLowerCase(Locale.ROOT));
        return start < 0 ? Optional.empty()
                : Optional.of(example.substring(0, start) + GAP + example.substring(start + word.length()));
    }

    /**
     * Проверяет, есть ли среди строк пустая.
     *
     * @param values строки
     * @return {@code true}, если хотя бы одной строки нет или она из пробелов
     */
    private static boolean isAnyBlank(String... values) {
        for (var value : values) {
            if (value == null || value.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
