package net.pamytno.deck.integration.ai.anthropic;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * JSON-схема ответа Claude при генерации вопросов.
 *
 * @param questions предложенные вопросы
 */
record QuestionsPayload(@JsonPropertyDescription("Вопросы для самопроверки") List<Item> questions) {

    /**
     * Один вопрос.
     *
     * @param text           текст вопроса
     * @param sourceFragment дословная цитата-источник
     */
    record Item(
            @JsonPropertyDescription("Вопрос для самопроверки") String text,
            @JsonPropertyDescription("Дословная цитата из материала, на которой основан вопрос") String sourceFragment
    ) {
    }
}
