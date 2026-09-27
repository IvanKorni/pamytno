package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.topic.TopicContentPrepared;
import net.pamytno.deck.config.DeckProperties;
import net.pamytno.deck.domain.TextChunk;
import net.pamytno.deck.domain.TextChunker;
import net.pamytno.deck.domain.TopicMaterial;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.repository.TextChunkRepository;
import net.pamytno.deck.repository.TopicMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Хранит фрагменты последней версии единого текста темы. Повторные и устаревшие версии
 * (доставка событий «как минимум один раз») пропускаются.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicMaterialService {

    private final TopicMaterialRepository materialRepository;
    private final TextChunkRepository chunkRepository;
    private final DeckProperties properties;
    private final Clock clock;

    /**
     * Принимает новую версию единого текста темы.
     *
     * @param event событие модуля {@code topic}
     */
    @Transactional
    public void accept(TopicContentPrepared event) {
        var topic = new TopicRef(event.topicId(), event.userId());
        var material = materialRepository.findByIdForUpdate(event.topicId());
        if (material.isPresent() && !material.get().isOlderThan(event.version())) {
            log.info("Версия [{}] текста темы [{}] уже учтена", event.version(), event.topicId());
            return;
        }
        material.ifPresentOrElse(known -> known.advanceTo(event.version(), clock.instant()),
                () -> materialRepository.save(new TopicMaterial(topic, event.version(), clock.instant())));
        var chunks = TextChunker.split(event.content(), properties.chunkSize()).stream()
                .map(span -> new TextChunk(topic, event.version(), span, clock.instant()))
                .toList();
        chunkRepository.saveAll(chunks);
        log.info("Тема [{}]: версия текста [{}] разбита на [{}] фрагментов",
                event.topicId(), event.version(), chunks.size());
    }

    /**
     * Фрагменты актуальной версии текста темы пользователя.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return фрагменты по порядку; пустой список, если текста нет или он пуст
     */
    @Transactional(readOnly = true)
    public List<TextChunk> latestChunks(UUID topicId, UUID userId) {
        return materialRepository.findByIdAndUserId(topicId, userId)
                .map(material -> chunkRepository.findAllByTopicIdAndContentVersionOrderBySequenceNumber(
                        topicId, material.getContentVersion()))
                .orElse(List.of());
    }
}
