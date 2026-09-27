package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.exception.NoApprovedQuestionsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link CardGenerationService}.
 */
@ExtendWith(MockitoExtension.class)
class CardGenerationServiceTest {

    @Mock
    private QuestionQueryService questionQueryService;
    @Mock
    private GenerationJobService jobService;
    @Mock
    private ApplicationEventPublisher events;
    @InjectMocks
    private CardGenerationService service;

    @Test
    @DisplayName("При одобренных вопросах генерация ставится в очередь")
    void start_publishesRequest() {
        var topic = DeckFixtures.topic();
        var question = new Question(DeckFixtures.chunk(topic, "Текст."), "Что?", "Текст.", Instant.EPOCH);
        var job = new GenerationJob(topic, GenerationJobType.CARDS, Instant.EPOCH);
        when(questionQueryService.list(topic.topicId(), topic.userId(), QuestionStatus.APPROVED))
                .thenReturn(List.of(question));
        when(jobService.start(topic, GenerationJobType.CARDS)).thenReturn(job);

        service.start(topic.userId(), topic.topicId());

        verify(events).publishEvent(new CardGenerationRequested(job.getId(), topic.topicId(), topic.userId()));
    }

    @Test
    @DisplayName("Без одобренных вопросов генерация отклоняется с NO_APPROVED_QUESTIONS")
    void start_throws_whenNoApprovedQuestions() {
        var topic = DeckFixtures.topic();
        when(questionQueryService.list(topic.topicId(), topic.userId(), QuestionStatus.APPROVED))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.start(topic.userId(), topic.topicId()))
                .isInstanceOf(NoApprovedQuestionsException.class);
        verifyNoInteractions(jobService, events);
    }
}
