package net.pamytno.deck.integration.ai.anthropic;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * JSON-схема ответа Claude при составлении карточки.
 *
 * @param front вопрос на лицевой стороне
 * @param back  ответ на оборотной стороне
 */
record CardPayload(
        @JsonPropertyDescription("Вопрос на лицевой стороне карточки") String front,
        @JsonPropertyDescription("Ответ по материалу с простой разметкой, не длиннее 10 предложений или пунктов")
        String back
) {
}
