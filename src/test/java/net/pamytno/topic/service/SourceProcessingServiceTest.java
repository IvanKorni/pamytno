package net.pamytno.topic.service;

import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.service.extraction.SourceTextExtractors;
import net.pamytno.topic.service.extraction.TextExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link SourceProcessingService}.
 */
@ExtendWith(MockitoExtension.class)
class SourceProcessingServiceTest {

    @Mock
    private SourceStateService stateService;
    @Mock
    private SourceTextExtractors extractors;
    @Mock
    private TopicMaterialRefresher refresher;

    private SourceProcessingService service;
    private Source source;

    @BeforeEach
    void setUp() {
        service = new SourceProcessingService(stateService, extractors, refresher,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var topic = Topic.create(UUID.randomUUID(), "JVM", null, Instant.EPOCH);
        source = Source.text(topic, SourceType.TEXT, null, "текст", Instant.EPOCH);
    }

    @Test
    @DisplayName("Извлечённый текст сохраняется, тема пересчитывается")
    void process_completesSourceAndRefreshesTopic() {
        when(stateService.startProcessing(source.getId())).thenReturn(Optional.of(source));
        when(extractors.extract(source)).thenReturn("текст");

        service.process(source.getId());

        verify(stateService).complete(source.getId(), "текст");
        verify(refresher).refresh(source.getTopicId());
    }

    @Test
    @DisplayName("Ошибка извлечения сохраняется с её кодом, тема всё равно пересчитывается")
    void process_failsSource_whenExtractionFails() {
        when(stateService.startProcessing(source.getId())).thenReturn(Optional.of(source));
        when(extractors.extract(source)).thenThrow(
                new TextExtractionException(SourceErrorCode.TRANSCRIPT_UNAVAILABLE, "Нет субтитров"));

        service.process(source.getId());

        verify(stateService).fail(source.getId(), SourceErrorCode.TRANSCRIPT_UNAVAILABLE, "Нет субтитров");
        verify(stateService, never()).complete(any(), any());
        verify(refresher).refresh(source.getTopicId());
    }

    @Test
    @DisplayName("Непредвиденная ошибка превращается в SOURCE_PROCESSING_FAILED")
    void process_failsSource_whenUnexpectedErrorOccurs() {
        when(stateService.startProcessing(source.getId())).thenReturn(Optional.of(source));
        when(extractors.extract(source)).thenThrow(new IllegalStateException("сбой"));

        service.process(source.getId());

        verify(stateService).fail(eq(source.getId()), eq(SourceErrorCode.SOURCE_PROCESSING_FAILED), any());
        verify(refresher).refresh(source.getTopicId());
    }

    @Test
    @DisplayName("Удалённый до начала обработки источник пропускается")
    void process_skips_whenSourceDeleted() {
        when(stateService.startProcessing(source.getId())).thenReturn(Optional.empty());

        service.process(source.getId());

        verifyNoInteractions(extractors, refresher);
    }
}
