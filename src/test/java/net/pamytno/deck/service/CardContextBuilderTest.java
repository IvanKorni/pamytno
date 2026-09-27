package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.repository.TextChunkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link CardContextBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class CardContextBuilderTest {

    @Mock
    private TextChunkRepository chunkRepository;
    @InjectMocks
    private CardContextBuilder builder;

    @Test
    @DisplayName("Контекст — цитата, дополненная фрагментом единого текста")
    void contextFor_appendsChunkToFragment() {
        var chunk = DeckFixtures.chunk(DeckFixtures.topic(), "JVM выполняет байткод. GC чистит память.");
        var question = new Question(chunk, "Что делает GC?", "GC чистит память.", Instant.EPOCH);
        when(chunkRepository.findById(chunk.getId())).thenReturn(Optional.of(chunk));

        assertThat(builder.contextFor(question))
                .isEqualTo("GC чистит память." + CardContextBuilder.SEPARATOR + chunk.getContent());
    }

    @Test
    @DisplayName("Если цитата и есть весь фрагмент, он не дублируется")
    void contextFor_doesNotDuplicate_whenFragmentIsWholeChunk() {
        var chunk = DeckFixtures.chunk(DeckFixtures.topic(), "JVM.");
        var question = new Question(chunk, "Что такое JVM?", "JVM.", Instant.EPOCH);
        when(chunkRepository.findById(chunk.getId())).thenReturn(Optional.of(chunk));

        assertThat(builder.contextFor(question)).isEqualTo("JVM.");
    }
}
