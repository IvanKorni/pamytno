package net.pamytno.deck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Настройки генерации через AI ({@code pamytno.deck.ai}). Ключи API задаются только
 * переменными окружения и сюда не попадают.
 *
 * @param provider          {@code stub} — детерминированная заглушка без сети,
 *                          {@code anthropic} — Claude через Anthropic API
 * @param questionsPerChunk сколько вопросов просить у модели на один фрагмент
 * @param anthropic         настройки провайдера Anthropic
 */
@ConfigurationProperties("pamytno.deck.ai")
public record AiProperties(
        @DefaultValue("stub") String provider,
        @DefaultValue("8") int questionsPerChunk,
        @DefaultValue Anthropic anthropic
) {

    /**
     * Настройки Claude.
     *
     * @param model     идентификатор модели
     * @param maxTokens максимум токенов ответа
     * @param effort    уровень усилий ({@code low}…{@code max}); пусто — значение API по умолчанию
     */
    public record Anthropic(
            @DefaultValue("claude-opus-5") String model,
            @DefaultValue("16000") long maxTokens,
            String effort
    ) {
    }
}
