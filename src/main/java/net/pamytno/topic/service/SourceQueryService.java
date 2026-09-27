package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.exception.SourceNotFoundException;
import net.pamytno.topic.repository.SourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Чтение источников пользователя.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SourceQueryService {

    private final TopicQueryService topicQueryService;
    private final SourceRepository sourceRepository;

    /**
     * Источники темы в порядке добавления.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return источники темы
     */
    public List<Source> list(UUID topicId, UUID userId) {
        topicQueryService.getOwned(topicId, userId);
        return sourceRepository.findAllByTopicIdOrderByCreatedAtAsc(topicId);
    }

    /**
     * Источник пользователя.
     *
     * @param sourceId идентификатор источника
     * @param userId   владелец
     * @return источник
     * @throws SourceNotFoundException если источника нет или он чужой
     */
    public Source getOwned(UUID sourceId, UUID userId) {
        return sourceRepository.findByIdAndUserId(sourceId, userId)
                .orElseThrow(() -> new SourceNotFoundException(sourceId));
    }
}
