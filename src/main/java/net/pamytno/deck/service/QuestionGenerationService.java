package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.exception.TopicContentNotReadyException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Запуск генерации вопросов: проверяет, что у темы есть текст, и ставит задачу в очередь.
 */
@Service
@RequiredArgsConstructor
public class QuestionGenerationService {

    private final TopicMaterialService topicMaterialService;
    private final GenerationJobService jobService;
    private final ApplicationEventPublisher events;

    /**
     * Запускает генерацию вопросов по актуальному тексту темы.
     *
     * @param userId  владелец темы
     * @param topicId идентификатор темы
     * @return задача в статусе PROCESSING
     * @throws TopicContentNotReadyException если у темы нет фрагментов текста
     */
    @Transactional
    public GenerationJob start(UUID userId, UUID topicId) {
        if (topicMaterialService.latestChunks(topicId, userId).isEmpty()) {
            throw new TopicContentNotReadyException(topicId);
        }
        var job = jobService.start(new TopicRef(topicId, userId), GenerationJobType.QUESTIONS);
        events.publishEvent(new QuestionGenerationRequested(job.getId(), topicId, userId));
        return job;
    }
}
