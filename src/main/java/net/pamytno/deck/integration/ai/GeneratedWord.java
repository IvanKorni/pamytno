package net.pamytno.deck.integration.ai;

/**
 * Английское слово или выражение, выбранное моделью из текста, с материалом для карточки.
 *
 * @param word               выражение в словарной форме
 * @param translation        русский перевод выражения
 * @param definition         объяснение значения на простом английском (B1)
 * @param example            английское предложение с выражением
 * @param answer             выражение в том виде, в каком оно стоит в предложении
 * @param exampleTranslation перевод предложения на русский
 */
public record GeneratedWord(String word, String translation, String definition, String example, String answer,
                            String exampleTranslation) {
}
