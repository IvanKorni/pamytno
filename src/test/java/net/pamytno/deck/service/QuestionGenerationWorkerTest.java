package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import net.pamytno.deck.integration.ai.GeneratedQuestion;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link QuestionGenerationWorker}.
 */
@ExtendWith(MockitoExtension.class)
class QuestionGenerationWorkerTest {

    @Mock
    private TopicMaterialService topicMaterialService;
    @Mock
    private AiProvider aiProvider;
    @Mock
    private QuestionWriter questionWriter;
    @Mock
    private GenerationJobService jobService;

    private QuestionGenerationWorker worker;
    private TopicRef topic;
    private QuestionGenerationRequested request;

    @BeforeEach
    void setUp() {
        worker = new QuestionGenerationWorker(topicMaterialService, aiProvider, questionWriter, jobService,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        topic = DeckFixtures.topic();
        request = new QuestionGenerationRequested(UUID.randomUUID(), topic.topicId(), topic.userId());
    }

    @Test
    @DisplayName("Вопросы по всем фрагментам сохраняются, задача завершается с их количеством")
    void generate_processesAllChunks() {
        var first = DeckFixtures.chunk(topic, "Первый.");
        var second = DeckFixtures.chunk(topic, "Второй.");
        var generated = List.of(new GeneratedQuestion("Вопрос?", "Первый."));
        when(topicMaterialService.latestChunks(topic.topicId(), topic.userId())).thenReturn(List.of(first, second));
        when(aiProvider.generateQuestions(any())).thenReturn(generated);
        when(questionWriter.save(any(), eq(generated))).thenReturn(2, 3);

        worker.generate(request);

        verify(jobService).complete(request.jobId(), 5);
    }

    @Test
    @DisplayName("Ошибка AI завершает задачу ошибкой, созданное до неё сохраняется")
    void generate_failsJob_whenAiFails() {
        var first = DeckFixtures.chunk(topic, "Первый.");
        var second = DeckFixtures.chunk(topic, "Второй.");
        when(topicMaterialService.latestChunks(topic.topicId(), topic.userId())).thenReturn(List.of(first, second));
        when(aiProvider.generateQuestions("Первый.")).thenReturn(List.of());
        when(aiProvider.generateQuestions("Второй.")).thenThrow(new AiGenerationException("лимит"));
        when(questionWriter.save(first, List.of())).thenReturn(0);

        worker.generate(request);

        verify(jobService).fail(eq(request.jobId()), eq(0), startsWith("AI не смог"));
        verify(jobService, never()).complete(any(), anyInt());
    }

    @Test
    @DisplayName("Непредвиденная ошибка тоже завершает задачу ошибкой")
    void generate_failsJob_whenUnexpectedError() {
        when(topicMaterialService.latestChunks(topic.topicId(), topic.userId()))
                .thenReturn(List.of(DeckFixtures.chunk(topic, "Текст.")));
        when(aiProvider.generateQuestions(any())).thenThrow(new IllegalStateException("сбой"));

        worker.generate(request);

        verify(jobService).fail(request.jobId(), 0, "Не удалось сгенерировать вопросы");
    }
}
