package net.pamytno.deck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Настройки модуля вопросов и карточек ({@code pamytno.deck}).
 *
 * @param chunkSize максимальная длина фрагмента текста, отправляемого в AI, в символах
 */
@ConfigurationProperties("pamytno.deck")
public record DeckProperties(@DefaultValue("6000") int chunkSize) {
}
