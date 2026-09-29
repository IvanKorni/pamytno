package net.pamytno.deck.domain;

import java.util.stream.Stream;

/**
 * Выражение для карточки слова в том виде, в каком его предложила модель, — до проверки и сборки пропуска.
 *
 * @param word               выражение в словарной форме
 * @param translation        русский перевод выражения
 * @param definition         объяснение значения на простом английском
 * @param example            английское предложение с выражением
 * @param answer             выражение в том виде, в каком оно стоит в предложении; может быть пустым
 * @param exampleTranslation перевод предложения на русский
 */
public record WordEntry(String word, String translation, String definition, String example, String answer,
                        String exampleTranslation) {

    /**
     * Проверяет, что есть всё, кроме необязательной формы в предложении.
     *
     * @return {@code true}, если какого-то обязательного поля нет или оно из пробелов
     */
    public boolean isIncomplete() {
        return Stream.of(word, translation, definition, example, exampleTranslation).anyMatch(WordEntry::isBlank);
    }

    /**
     * Форма выражения в предложении без пробелов по краям.
     *
     * @return форма или пустая строка, если модель её не указала
     */
    public String answerOrEmpty() {
        return answer == null ? "" : answer.strip();
    }

    /**
     * Проверяет строку на пустоту.
     *
     * @param value строка
     * @return {@code true}, если строки нет или она из пробелов
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
