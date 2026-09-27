package net.pamytno.topic.service;

import net.pamytno.topic.PdfFixtures;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.exception.UnsupportedFileTypeException;
import net.pamytno.topic.integration.storage.LocalFileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link PdfSourceSubmissionService}.
 */
@ExtendWith(MockitoExtension.class)
class PdfSourceSubmissionServiceTest {

    @Mock
    private TopicQueryService topicQueryService;
    @Mock
    private SourceRegistrar sourceRegistrar;
    @Mock
    private LocalFileStorage storage;

    private PdfSourceSubmissionService service;

    @BeforeEach
    void setUp() {
        service = new PdfSourceSubmissionService(topicQueryService, sourceRegistrar, storage,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("PDF сохраняется в хранилище по ключу источника и регистрируется")
    void submit_storesFileAndRegistersSource() {
        // given
        var userId = UUID.randomUUID();
        var topic = Topic.create(userId, "JVM", null, Instant.EPOCH);
        var content = PdfFixtures.withText("JVM");
        when(topicQueryService.getOwned(topic.getId(), userId)).thenReturn(topic);
        when(sourceRegistrar.register(eq(topic), any(Source.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        var source = service.submit(userId, topic.getId(), "notes.pdf", content);

        // then
        assertThat(source.getType()).isEqualTo(SourceType.PDF);
        assertThat(source.getOriginalName()).isEqualTo("notes.pdf");
        assertThat(source.getStorageKey()).isEqualTo(topic.getId() + "/" + source.getId() + ".pdf");
        verify(storage).save(source.getStorageKey(), content);
    }

    @Test
    @DisplayName("Файл без сигнатуры PDF отклоняется с UNSUPPORTED_FILE_TYPE до сохранения")
    void submit_rejectsNonPdf() {
        var content = "просто текст".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> service.submit(UUID.randomUUID(), UUID.randomUUID(), "notes.txt", content))
                .isInstanceOf(UnsupportedFileTypeException.class);
        verifyNoInteractions(storage, sourceRegistrar);
    }
}
