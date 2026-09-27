package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.exception.TopicContentNotReadyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link QuestionGenerationService}.
 */
@ExtendWith(MockitoExtension.class)
class QuestionGenerationServiceTest {

    @Mock
    private TopicMaterialService topicMaterialService;
    @Mock
    private GenerationJobService jobService;
    @Mock
    private ApplicationEventPublisher events;
    @InjectMocks
    private QuestionGenerationService service;

    @Test
    @DisplayName("Генерация ставится в очередь внутренним событием с идентификатором задачи")
    void start_publishesGenerationRequest() {
        var topic = DeckFixtures.topic();
        var job = new GenerationJob(topic, GenerationJobType.QUESTIONS, Instant.EPOCH);
        when(topicMaterialService.latestChunks(topic.topicId(), topic.userId()))
                .thenReturn(List.of(DeckFixtures.chunk(topic, "текст")));
        when(jobService.start(topic, GenerationJobType.QUESTIONS)).thenReturn(job);

        var result = service.start(topic.userId(), topic.topicId());

        assertThat(result).isSameAs(job);
        verify(events).publishEvent(new QuestionGenerationRequested(job.getId(), topic.topicId(), topic.userId()));
    }

    @Test
    @DisplayName("Без текста темы генерация отклоняется с TOPIC_CONTENT_NOT_READY")
    void start_throws_whenNoChunks() {
        var topic = DeckFixtures.topic();
        when(topicMaterialService.latestChunks(topic.topicId(), topic.userId())).thenReturn(List.of());

        assertThatThrownBy(() -> service.start(topic.userId(), topic.topicId()))
                .isInstanceOf(TopicContentNotReadyException.class);
        verifyNoInteractions(jobService);
        verify(events, never()).publishEvent(any());
    }
}
