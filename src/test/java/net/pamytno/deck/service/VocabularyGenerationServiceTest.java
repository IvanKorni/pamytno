package net.pamytno.deck.service;

import net.pamytno.deck.DeckFixtures;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.exception.TopicNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link VocabularyGenerationService}.
 */
@ExtendWith(MockitoExtension.class)
class VocabularyGenerationServiceTest {

    @Mock
    private TopicMaterialService topicMaterialService;
    @Mock
    private GenerationJobService jobService;
    @Mock
    private ApplicationEventPublisher events;

    private VocabularyGenerationService service;

    @BeforeEach
    void setUp() {
        service = new VocabularyGenerationService(topicMaterialService, jobService, events);
    }

    @Test
    @DisplayName("Задача запускается для своей темы и передаёт текст с инструкцией воркеру")
    void start_publishesRequest() {
        // given
        var topic = DeckFixtures.topic();
        var job = new GenerationJob(topic, GenerationJobType.VOCABULARY, Instant.EPOCH);
        when(topicMaterialService.requireTopic(topic.topicId(), topic.userId())).thenReturn(topic);
        when(jobService.start(topic, GenerationJobType.VOCABULARY)).thenReturn(job);

        // when
        var started = service.start(topic.userId(), topic.topicId(), "коллокации", "make a decision");

        // then
        assertThat(started).isSameAs(job);
        verify(events).publishEvent(new VocabularyGenerationRequested(job.getId(), topic.topicId(), topic.userId(),
                "коллокации", "make a decision"));
    }

    @Test
    @DisplayName("Чужая или неизвестная тема — TopicNotFoundException, задача не создаётся")
    void start_rejectsUnknownTopic() {
        // given
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(topicMaterialService.requireTopic(topicId, userId)).thenThrow(new TopicNotFoundException(topicId));

        // when / then
        assertThatThrownBy(() -> service.start(userId, topicId, null, "word"))
                .isInstanceOf(TopicNotFoundException.class);
        verify(jobService, never()).start(any(), any());
        verify(events, never()).publishEvent(any());
    }
}
