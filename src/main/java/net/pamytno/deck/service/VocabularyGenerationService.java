package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.exception.GenerationInProgressException;
import net.pamytno.deck.exception.TopicNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Запуск составления карточек слов: проверяет владельца темы и ставит задачу в очередь. Текст темы не нужен —
 * слова берутся из переданного текста.
 */
@Service
@RequiredArgsConstructor
public class VocabularyGenerationService {

    private final TopicMaterialService topicMaterialService;
    private final GenerationJobService jobService;
    private final ApplicationEventPublisher events;

    /**
     * Запускает составление карточек слов.
     *
     * @param userId      владелец темы
     * @param topicId     идентификатор темы
     * @param instruction что взять из текста; может быть пустой
     * @param text        слова, список или текст
     * @return задача в статусе PROCESSING
     * @throws TopicNotFoundException        если темы нет или она чужая
     * @throws GenerationInProgressException если по теме уже составляются карточки слов
     */
    @Transactional
    public GenerationJob start(UUID userId, UUID topicId, String instruction, String text) {
        var topic = topicMaterialService.requireTopic(topicId, userId);
        var job = jobService.start(topic, GenerationJobType.VOCABULARY);
        events.publishEvent(new VocabularyGenerationRequested(job.getId(), topicId, userId, instruction, text));
        return job;
    }
}
