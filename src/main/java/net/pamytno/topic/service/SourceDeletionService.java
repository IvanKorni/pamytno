package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.repository.SourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Удаляет источник и пересчитывает тему без него.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDeletionService {

    private final SourceQueryService sourceQueryService;
    private final SourceRepository sourceRepository;
    private final TopicMaterialRefresher materialRefresher;

    /**
     * Удаляет источник пользователя.
     *
     * @param sourceId идентификатор источника
     * @param userId   владелец
     */
    @Transactional
    public void delete(UUID sourceId, UUID userId) {
        var source = sourceQueryService.getOwned(sourceId, userId);
        sourceRepository.delete(source);
        materialRefresher.refresh(source.getTopicId());
        log.info("Источник [{}] удалён из темы [{}]", sourceId, source.getTopicId());
    }
}
