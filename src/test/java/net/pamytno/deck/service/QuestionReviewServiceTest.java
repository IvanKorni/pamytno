package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionDecision;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.exception.QuestionNotFoundException;
import net.pamytno.deck.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link QuestionReviewService}.
 */
@ExtendWith(MockitoExtension.class)
class QuestionReviewServiceTest {

    @Mock
    private QuestionQueryService questionQueryService;
    @Mock
    private QuestionRepository questionRepository;

    private QuestionReviewService service;

    @BeforeEach
    void setUp() {
        service = new QuestionReviewService(questionQueryService, questionRepository,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Массовое решение применяется ко всем найденным вопросам")
    void decideAll_appliesDecisionToAll() {
        var chunk = DeckFixtures.chunk(DeckFixtures.topic(), "Текст.");
        var first = new Question(chunk, "Первый?", "Текст.", Instant.EPOCH);
        var second = new Question(chunk, "Второй?", "Текст.", Instant.EPOCH);
        var ids = List.of(first.getId(), second.getId());
        when(questionRepository.findAllByIdInAndUserId(ids, chunk.getUserId())).thenReturn(List.of(first, second));

        var result = service.decideAll(ids, chunk.getUserId(), QuestionDecision.APPROVE);

        assertThat(result).extracting(Question::getStatus).containsOnly(QuestionStatus.APPROVED);
    }

    @Test
    @DisplayName("Если хоть один вопрос не найден, ничего не меняется и отвечается 404")
    void decideAll_throws_whenAnyQuestionMissing() {
        var chunk = DeckFixtures.chunk(DeckFixtures.topic(), "Текст.");
        var found = new Question(chunk, "Есть?", "Текст.", Instant.EPOCH);
        var missing = UUID.randomUUID();
        var ids = List.of(found.getId(), missing);
        when(questionRepository.findAllByIdInAndUserId(ids, chunk.getUserId())).thenReturn(List.of(found));

        assertThatThrownBy(() -> service.decideAll(ids, chunk.getUserId(), QuestionDecision.REJECT))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining(missing.toString());
        assertThat(found.getStatus()).isEqualTo(QuestionStatus.GENERATED);
    }
}
