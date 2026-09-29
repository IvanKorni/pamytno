package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.domain.WordCard;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link VocabularyGenerationWorker}.
 */
@ExtendWith(MockitoExtension.class)
class VocabularyGenerationWorkerTest {

    @Mock
    private AiProvider aiProvider;
    @Mock
    private FlashcardWriter flashcardWriter;
    @Mock
    private GenerationJobService jobService;

    private VocabularyGenerationWorker worker;
    private TopicRef topic;
    private VocabularyGenerationRequested request;

    @BeforeEach
    void setUp() {
        worker = new VocabularyGenerationWorker(aiProvider, flashcardWriter, jobService,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        topic = DeckFixtures.topic();
        request = new VocabularyGenerationRequested(UUID.randomUUID(), topic.topicId(), topic.userId(),
                "слова, выделенные жирным", "The **contract** was **reliable**.");
    }

    @Test
    @DisplayName("Карточка создаётся на каждое полное выражение, неполное пропускается")
    void generate_createsCardPerCompleteWord() {
        // given
        when(aiProvider.generateVocabulary(request.instruction(), request.text())).thenReturn(List.of(
                new GeneratedWord("contract", "договор", "A formal agreement.", "We signed a contract.",
                        "contract", "Мы подписали договор."),
                new GeneratedWord("reliable", "", "You can trust it.", "A reliable car.", "reliable",
                        "Надёжная машина.")));

        // when
        worker.generate(request);

        // then
        var card = ArgumentCaptor.forClass(WordCard.class);
        verify(flashcardWriter).createWord(eq(topic), card.capture());
        assertThat(card.getValue().word()).isEqualTo("contract");
        assertThat(card.getValue().example()).isEqualTo("We signed a co_____.");
        verify(jobService).complete(request.jobId(), 1);
    }

    @Test
    @DisplayName("Ошибка AI завершает задачу ошибкой без карточек")
    void generate_failsJob_whenAiFails() {
        // given
        when(aiProvider.generateVocabulary(any(), any())).thenThrow(new AiGenerationException("недоступна"));

        // when
        worker.generate(request);

        // then
        verify(flashcardWriter, never()).createWord(any(), any());
        verify(jobService).fail(eq(request.jobId()), eq(0), startsWith("AI не смог составить карточки слов"));
    }

    @Test
    @DisplayName("Непредвиденная ошибка записи завершает задачу ошибкой")
    void generate_failsJob_whenWriterFails() {
        // given
        when(aiProvider.generateVocabulary(any(), any())).thenReturn(List.of(new GeneratedWord("contract",
                "договор", "A formal agreement.", "We signed a contract.", "contract", "Мы подписали договор.")));
        doThrow(new IllegalStateException("БД недоступна"))
                .when(flashcardWriter).createWord(any(), any());

        // when
        worker.generate(request);

        // then
        verify(jobService).fail(request.jobId(), 0, "Не удалось составить карточки слов");
    }
}
