package net.pamytno.deck.service;

import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.common.event.topic.TopicCreated;
import net.pamytno.deck.config.DeckProperties;
import net.pamytno.deck.domain.TextChunk;
import net.pamytno.deck.domain.TopicMaterial;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.repository.TextChunkRepository;
import net.pamytno.deck.repository.TopicMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link TopicMaterialService}.
 */
@ExtendWith(MockitoExtension.class)
class TopicMaterialServiceTest {

    private static final TopicRef TOPIC = new TopicRef(UUID.randomUUID(), UUID.randomUUID());

    @Mock
    private TopicMaterialRepository materialRepository;
    @Mock
    private TextChunkRepository chunkRepository;

    private TopicMaterialService service;

    @BeforeEach
    void setUp() {
        service = new TopicMaterialService(materialRepository, chunkRepository, new DeckProperties(100),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Первая версия текста сохраняет проекцию темы и фрагменты")
    @SuppressWarnings("unchecked")
    void accept_storesFirstVersion() {
        when(materialRepository.findByIdForUpdate(TOPIC.topicId())).thenReturn(Optional.empty());

        service.accept(new TopicContentPrepared(TOPIC.topicId(), TOPIC.userId(), 1, "JVM выполняет байткод."));

        var chunks = ArgumentCaptor.forClass(List.class);
        verify(materialRepository).save(any(TopicMaterial.class));
        verify(chunkRepository).saveAll(chunks.capture());
        assertThat((List<TextChunk>) chunks.getValue()).singleElement()
                .satisfies(chunk -> assertThat(chunk.getContentVersion()).isEqualTo(1));
    }

    @Test
    @DisplayName("Уже учтённая или более старая версия пропускается")
    void accept_skipsKnownVersion() {
        var material = new TopicMaterial(TOPIC, 3, Instant.EPOCH);
        when(materialRepository.findByIdForUpdate(TOPIC.topicId())).thenReturn(Optional.of(material));

        service.accept(new TopicContentPrepared(TOPIC.topicId(), TOPIC.userId(), 3, "текст"));
        service.accept(new TopicContentPrepared(TOPIC.topicId(), TOPIC.userId(), 2, "старый текст"));

        verify(chunkRepository, never()).saveAll(any());
        assertThat(material.getContentVersion()).isEqualTo(3);
    }

    @Test
    @DisplayName("Пустой текст продвигает версию без фрагментов")
    void accept_advancesVersion_forEmptyText() {
        var material = new TopicMaterial(TOPIC, 1, Instant.EPOCH);
        when(materialRepository.findByIdForUpdate(TOPIC.topicId())).thenReturn(Optional.of(material));

        service.accept(new TopicContentPrepared(TOPIC.topicId(), TOPIC.userId(), 2, ""));

        assertThat(material.getContentVersion()).isEqualTo(2);
        verify(chunkRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("Новая тема регистрируется без текста")
    void register_storesTopicWithoutContent() {
        // given
        when(materialRepository.findByIdForUpdate(TOPIC.topicId())).thenReturn(Optional.empty());

        // when
        service.register(new TopicCreated(TOPIC.topicId(), TOPIC.userId()));

        // then
        var saved = ArgumentCaptor.forClass(TopicMaterial.class);
        verify(materialRepository).save(saved.capture());
        assertThat(saved.getValue().topic()).isEqualTo(TOPIC);
        assertThat(saved.getValue().getContentVersion()).isEqualTo(TopicMaterial.NO_CONTENT);
    }

    @Test
    @DisplayName("Повторная регистрация не откатывает уже пришедший текст")
    void register_keepsKnownTopic() {
        // given
        var material = new TopicMaterial(TOPIC, 2, Instant.EPOCH);
        when(materialRepository.findByIdForUpdate(TOPIC.topicId())).thenReturn(Optional.of(material));

        // when
        service.register(new TopicCreated(TOPIC.topicId(), TOPIC.userId()));

        // then
        verify(materialRepository, never()).save(any());
        assertThat(material.getContentVersion()).isEqualTo(2);
    }
}
