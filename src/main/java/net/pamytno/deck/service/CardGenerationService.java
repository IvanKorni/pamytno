package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.exception.NoApprovedQuestionsException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Запуск генерации карточек: проверяет, что есть одобренные вопросы, и ставит задачу в очередь.
 */
@Service
@RequiredArgsConstructor
public class CardGenerationService {

    private final QuestionQueryService questionQueryService;
    private final GenerationJobService jobService;
    private final ApplicationEventPublisher events;

    /**
     * Запускает генерацию карточек по одобренным вопросам темы.
     *
     * @param userId  владелец темы
     * @param topicId идентификатор темы
     * @return задача в статусе PROCESSING
     * @throws NoApprovedQuestionsException если одобренных вопросов нет
     */
    @Transactional
    public GenerationJob start(UUID userId, UUID topicId) {
        if (questionQueryService.list(topicId, userId, QuestionStatus.APPROVED).isEmpty()) {
            throw new NoApprovedQuestionsException(topicId);
        }
        var job = jobService.start(new TopicRef(topicId, userId), GenerationJobType.CARDS);
        events.publishEvent(new CardGenerationRequested(job.getId(), topicId, userId));
        return job;
    }
}
