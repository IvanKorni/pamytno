package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.exception.TopicNotFoundException;
import net.pamytno.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Чтение тем пользователя. Чужая тема неотличима от несуществующей.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicQueryService {

    private final TopicRepository topicRepository;

    /**
     * Темы пользователя, новые сверху.
     *
     * @param userId владелец
     * @return список тем
     */
    public List<Topic> list(UUID userId) {
        return topicRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Тема пользователя.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return тема
     * @throws TopicNotFoundException если темы нет или она чужая
     */
    public Topic getOwned(UUID topicId, UUID userId) {
        return topicRepository.findByIdAndUserId(topicId, userId)
                .orElseThrow(() -> new TopicNotFoundException(topicId));
    }
}
