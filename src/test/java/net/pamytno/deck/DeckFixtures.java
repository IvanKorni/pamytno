package net.pamytno.deck;

import net.pamytno.deck.domain.ChunkSpan;
import net.pamytno.deck.domain.TextChunk;
import net.pamytno.deck.domain.TopicRef;

import java.time.Instant;
import java.util.UUID;

/**
 * Тестовые данные модуля {@code deck}.
 */
public final class DeckFixtures {

    /**
     * Запрещает создание экземпляров.
     */
    private DeckFixtures() {
    }

    /**
     * Случайная тема со случайным владельцем.
     *
     * @return ссылка на тему
     */
    public static TopicRef topic() {
        return new TopicRef(UUID.randomUUID(), UUID.randomUUID());
    }

    /**
     * Фрагмент первой версии текста темы.
     *
     * @param topic   тема
     * @param content текст фрагмента
     * @return фрагмент
     */
    public static TextChunk chunk(TopicRef topic, String content) {
        return new TextChunk(topic, 1, new ChunkSpan(1, 0, content.length(), content), Instant.EPOCH);
    }
}
