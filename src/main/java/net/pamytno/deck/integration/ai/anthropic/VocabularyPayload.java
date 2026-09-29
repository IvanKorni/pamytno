package net.pamytno.deck.integration.ai.anthropic;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * JSON-схема ответа Claude при составлении карточек английских слов.
 *
 * @param cards выражения для карточек
 */
record VocabularyPayload(@JsonPropertyDescription("Карточки слов в порядке появления в материале")
                         List<Item> cards) {

    /**
     * Одно выражение.
     *
     * @param word               выражение в словарной форме
     * @param translation        русский перевод
     * @param definition         объяснение на простом английском
     * @param example            предложение с выражением
     * @param answer             выражение в том виде, в каком оно стоит в предложении
     * @param exampleTranslation перевод предложения
     */
    record Item(
            @JsonPropertyDescription("Английское выражение в словарной форме") String word,
            @JsonPropertyDescription("Короткий русский перевод") String translation,
            @JsonPropertyDescription("Объяснение значения простым английским (B1) без самого выражения")
            String definition,
            @JsonPropertyDescription("Английское предложение с выражением, без пропусков") String example,
            @JsonPropertyDescription("Выражение ровно в том виде, в каком оно стоит в example") String answer,
            @JsonPropertyDescription("Перевод предложения на русский") String exampleTranslation
    ) {
    }
}
