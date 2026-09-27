package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.common.event.topic.TopicDeleted;
import net.pamytno.topic.repository.TopicRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Удаляет тему и сообщает другим модулям, что всё построенное по ней больше не нужно.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicDeletionService {

    private final TopicRepository topicRepository;
    private final TopicQueryService topicQueryService;
    private final ApplicationEventPublisher events;

    /**
     * Удаляет тему пользователя и публикует {@link TopicDeleted}.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     */
    @Transactional
    public void delete(UUID topicId, UUID userId) {
        var topic = topicQueryService.getOwned(topicId, userId);
        topicRepository.delete(topic);
        events.publishEvent(new TopicDeleted(topicId, userId));
        log.info("Тема [{}] удалена пользователем [{}]", topicId, userId);
    }
}
