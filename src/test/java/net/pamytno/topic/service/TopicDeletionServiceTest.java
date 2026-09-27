package net.pamytno.topic.service;

import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.exception.TopicNotFoundException;
import net.pamytno.topic.repository.TopicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link TopicDeletionService}.
 */
@ExtendWith(MockitoExtension.class)
class TopicDeletionServiceTest {

    @Mock
    private TopicRepository topicRepository;
    @Mock
    private TopicQueryService topicQueryService;
    @Mock
    private ApplicationEventPublisher events;
    @InjectMocks
    private TopicDeletionService service;

    @Test
    @DisplayName("Удаление темы публикует TopicDeleted")
    void delete_removesTopicAndPublishesEvent() {
        // given
        var userId = UUID.randomUUID();
        var topic = Topic.create(userId, "JVM", null, Instant.EPOCH);
        when(topicQueryService.getOwned(topic.getId(), userId)).thenReturn(topic);

        // when
        service.delete(topic.getId(), userId);

        // then
        verify(topicRepository).delete(topic);
        verify(events).publishEvent(new TopicDeleted(topic.getId(), userId));
    }

    @Test
    @DisplayName("Чужую или несуществующую тему удалить нельзя, событие не публикуется")
    void delete_throwsNotFound_whenTopicNotOwned() {
        // given
        var topicId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(topicQueryService.getOwned(topicId, userId)).thenThrow(new TopicNotFoundException(topicId));

        // when / then
        assertThatThrownBy(() -> service.delete(topicId, userId)).isInstanceOf(TopicNotFoundException.class);
        verify(events, never()).publishEvent(any());
    }
}
