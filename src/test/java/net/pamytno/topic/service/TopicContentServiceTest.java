package net.pamytno.topic.service;

import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.domain.TopicContent;
import net.pamytno.topic.repository.TopicContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link TopicContentService}.
 */
@ExtendWith(MockitoExtension.class)
class TopicContentServiceTest {

    @Mock
    private TopicContentRepository contentRepository;
    @Mock
    private ApplicationEventPublisher events;

    private TopicContentService service;
    private Topic topic;

    @BeforeEach
    void setUp() {
        service = new TopicContentService(contentRepository, events, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        topic = Topic.create(UUID.randomUUID(), "JVM", null, Instant.EPOCH);
    }

    @Test
    @DisplayName("Первый готовый текст сохраняется версией 1 и публикуется событием")
    void rebuild_createsFirstVersion() {
        when(contentRepository.findFirstByTopicIdOrderByVersionDesc(topic.getId())).thenReturn(Optional.empty());
        when(contentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.rebuild(topic, List.of(readySource("текст")));

        verify(events).publishEvent(new TopicContentPrepared(topic.getId(), topic.getUserId(), 1, "текст"));
    }

    @Test
    @DisplayName("Изменившийся текст сохраняется следующей версией")
    void rebuild_createsNextVersion_whenTextChanged() {
        var previous = TopicContent.first(topic.getId(), "старый", Instant.EPOCH);
        when(contentRepository.findFirstByTopicIdOrderByVersionDesc(topic.getId())).thenReturn(Optional.of(previous));
        when(contentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.rebuild(topic, List.of(readySource("новый")));

        verify(events).publishEvent(new TopicContentPrepared(topic.getId(), topic.getUserId(), 2, "новый"));
    }

    @Test
    @DisplayName("Тот же текст не создаёт новую версию")
    void rebuild_skips_whenTextUnchanged() {
        var previous = TopicContent.first(topic.getId(), "текст", Instant.EPOCH);
        when(contentRepository.findFirstByTopicIdOrderByVersionDesc(topic.getId())).thenReturn(Optional.of(previous));

        service.rebuild(topic, List.of(readySource("текст")));

        verify(contentRepository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Пустой текст без предыдущих версий не сохраняется")
    void rebuild_skips_whenNothingReadyYet() {
        when(contentRepository.findFirstByTopicIdOrderByVersionDesc(topic.getId())).thenReturn(Optional.empty());

        service.rebuild(topic, List.of());

        verify(contentRepository, never()).save(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Если готовых источников не осталось, публикуется пустая версия")
    void rebuild_publishesEmptyVersion_whenAllSourcesRemoved() {
        var previous = TopicContent.first(topic.getId(), "текст", Instant.EPOCH);
        when(contentRepository.findFirstByTopicIdOrderByVersionDesc(topic.getId())).thenReturn(Optional.of(previous));
        when(contentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.rebuild(topic, List.of());

        verify(events).publishEvent(new TopicContentPrepared(topic.getId(), topic.getUserId(), 2, ""));
    }

    /**
     * Создаёт обработанный источник темы.
     *
     * @param text извлечённый текст
     * @return источник в статусе READY
     */
    private Source readySource(String text) {
        var source = Source.text(topic, SourceType.TEXT, null, text, Instant.EPOCH);
        source.completeExtraction(text, Instant.EPOCH);
        return source;
    }
}
