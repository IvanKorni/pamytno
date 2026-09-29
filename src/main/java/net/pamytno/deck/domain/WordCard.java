package net.pamytno.deck.domain;

import java.util.Optional;

/**
 * Карточка английского слова или выражения. На лицевой стороне — объяснение на простом английском и
 * предложение с пропуском и подсказкой, на оборотной — само выражение, русский перевод и перевод предложения.
 *
 * @param word               выражение в словарной форме
 * @param translation        русский перевод выражения
 * @param definition         объяснение значения на простом английском
 * @param example            предложение с пропуском, например «I had to ma_____ quickly»
 * @param exampleTranslation перевод предложения на русский
 */
public record WordCard(String word, String translation, String definition, String example,
                       String exampleTranslation) {

    /**
     * Проверяет ответ модели и собирает предложение с пропуском на месте выражения.
     *
     * @param entry выражение от модели
     * @return карточка или пустое значение, если поля нет или выражения в предложении не нашлось
     */
    public static Optional<WordCard> from(WordEntry entry) {
        if (entry.isIncomplete()) {
            return Optional.empty();
        }
        return WordGap.apply(entry.example().strip(), entry.answerOrEmpty(), entry.word().strip())
                .map(gapped -> new WordCard(entry.word().strip(), entry.translation().strip(),
                        entry.definition().strip(), gapped, entry.exampleTranslation().strip()));
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
}
