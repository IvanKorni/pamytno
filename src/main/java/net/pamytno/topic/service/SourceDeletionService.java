package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.repository.SourceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Удаляет источник, пересчитывает тему без него и освобождает файл в хранилище.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceDeletionService {

    private final SourceQueryService sourceQueryService;
    private final SourceRepository sourceRepository;
    private final TopicMaterialRefresher materialRefresher;
    private final ApplicationEventPublisher events;

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
        if (source.getStorageKey() != null) {
            events.publishEvent(new StoragePathObsolete(source.getStorageKey()));
        }
        log.info("Источник [{}] удалён из темы [{}]", sourceId, source.getTopicId());
    }
}
