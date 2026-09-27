package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.TopicContent;
import net.pamytno.topic.exception.TopicContentNotFoundException;
import net.pamytno.topic.repository.TopicContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Чтение единого текста темы.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicContentQueryService {

    private final TopicQueryService topicQueryService;
    private final TopicContentRepository contentRepository;

    /**
     * Последняя версия единого текста темы пользователя.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return последняя версия
     * @throws TopicContentNotFoundException если текст ещё не собирался
     */
    public TopicContent getLatest(UUID topicId, UUID userId) {
        topicQueryService.getOwned(topicId, userId);
        return contentRepository.findFirstByTopicIdOrderByVersionDesc(topicId)
                .orElseThrow(() -> new TopicContentNotFoundException(topicId));
    }
}
