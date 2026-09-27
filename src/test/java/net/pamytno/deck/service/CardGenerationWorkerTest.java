package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedCard;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link CardGenerationWorker}.
 */
@ExtendWith(MockitoExtension.class)
class CardGenerationWorkerTest {

    @Mock
    private QuestionQueryService questionQueryService;
    @Mock
    private CardContextBuilder contextBuilder;
    @Mock
    private AiProvider aiProvider;
    @Mock
    private FlashcardWriter flashcardWriter;
    @Mock
    private GenerationJobService jobService;

    private CardGenerationWorker worker;
    private TopicRef topic;
    private CardGenerationRequested request;

    @BeforeEach
    void setUp() {
        worker = new CardGenerationWorker(questionQueryService, contextBuilder, aiProvider, flashcardWriter,
                jobService, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        topic = DeckFixtures.topic();
        request = new CardGenerationRequested(UUID.randomUUID(), topic.topicId(), topic.userId());
    }

    @Test
    @DisplayName("Карточка создаётся по каждому одобренному вопросу, задача считает только созданные")
    void generate_createsCardPerQuestion() {
        var first = question("Первый?");
        var second = question("Второй?");
        var card = new GeneratedCard("Вопрос?", "Ответ.");
        when(questionQueryService.list(topic.topicId(), topic.userId(), QuestionStatus.APPROVED))
                .thenReturn(List.of(first, second));
        when(contextBuilder.contextFor(any())).thenReturn("контекст");
        when(aiProvider.generateCard(any(), eq("контекст"))).thenReturn(card);
        when(flashcardWriter.create(first.getId(), card)).thenReturn(true);
        when(flashcardWriter.create(second.getId(), card)).thenReturn(false);

        worker.generate(request);

        verify(jobService).complete(request.jobId(), 1);
    }

    @Test
    @DisplayName("Ошибка AI завершает задачу ошибкой")
    void generate_failsJob_whenAiFails() {
        when(questionQueryService.list(topic.topicId(), topic.userId(), QuestionStatus.APPROVED))
                .thenReturn(List.of(question("Вопрос?")));
        when(contextBuilder.contextFor(any())).thenReturn("контекст");
        when(aiProvider.generateCard(any(), any())).thenThrow(new AiGenerationException("недоступен"));

        worker.generate(request);

        verify(jobService).fail(eq(request.jobId()), eq(0), startsWith("AI не смог"));
    }

    /**
     * Одобренный вопрос темы.
     *
     * @param text текст вопроса
     * @return вопрос в статусе APPROVED
     */
    private Question question(String text) {
        var question = new Question(DeckFixtures.chunk(topic, "Текст."), text, "Текст.", Instant.EPOCH);
        question.approve(Instant.EPOCH);
        return question;
    }
}
