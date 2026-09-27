package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
import net.pamytno.deck.repository.QuestionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit-тесты {@link QuestionWriter}.
 */
@ExtendWith(MockitoExtension.class)
class QuestionWriterTest {

    @Mock
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("Пустые вопросы отбрасываются, без цитаты источником становится весь фрагмент")
    @SuppressWarnings("unchecked")
    void save_filtersBlankAndFillsFragment() {
        var writer = new QuestionWriter(questionRepository, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var chunk = DeckFixtures.chunk(DeckFixtures.topic(), "JVM выполняет байткод.");

        var saved = writer.save(chunk, List.of(
                new GeneratedQuestion(" Что выполняет JVM? ", "JVM выполняет байткод."),
                new GeneratedQuestion("  ", "цитата"),
                new GeneratedQuestion("Зачем JVM?", null)));

        var captor = ArgumentCaptor.forClass(List.class);
        verify(questionRepository).saveAll(captor.capture());
        var questions = (List<Question>) captor.getValue();
        assertThat(saved).isEqualTo(2);
        assertThat(questions).extracting(Question::getText).containsExactly("Что выполняет JVM?", "Зачем JVM?");
        assertThat(questions).extracting(Question::getSourceFragment)
                .containsExactly("JVM выполняет байткод.", "JVM выполняет байткод.");
        assertThat(questions).allSatisfy(question -> {
            assertThat(question.getChunkId()).isEqualTo(chunk.getId());
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.GENERATED);
        });
    }
}
