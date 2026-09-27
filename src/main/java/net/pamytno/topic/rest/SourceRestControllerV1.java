package net.pamytno.topic.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.topic.mapper.SourceMapper;
import net.pamytno.topic.rest.api.SourcesApi;
import net.pamytno.topic.rest.dto.SourceDto;
import net.pamytno.topic.rest.dto.SourceTextDto;
import net.pamytno.topic.service.SourceDeletionService;
import net.pamytno.topic.service.SourceQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Чтение и удаление источников ({@code /api/topics/{topicId}/sources}, {@code /api/sources/{sourceId}}).
 */
@RestController
@RequiredArgsConstructor
public class SourceRestControllerV1 implements SourcesApi {

    private final SourceQueryService sourceQueryService;
    private final SourceDeletionService sourceDeletionService;
    private final SourceMapper sourceMapper;
    private final CurrentUser currentUser;

    /**
     * Источники темы.
     *
     * @param topicId идентификатор темы
     * @return 200 и источники в порядке добавления
     */
    @Override
    public ResponseEntity<List<SourceDto>> listSources(UUID topicId) {
        return ResponseEntity.ok(sourceMapper.toDtos(sourceQueryService.list(topicId, currentUser.id())));
    }

    /**
     * Источник по идентификатору — для опроса статуса обработки.
     *
     * @param sourceId идентификатор источника
     * @return 200 и источник
     */
    @Override
    public ResponseEntity<SourceDto> getSource(UUID sourceId) {
        return ResponseEntity.ok(sourceMapper.toDto(sourceQueryService.getOwned(sourceId, currentUser.id())));
    }

    /**
     * Исходный и извлечённый тексты источника.
     *
     * @param sourceId идентификатор источника
     * @return 200 и тексты
     */
    @Override
    public ResponseEntity<SourceTextDto> getSourceText(UUID sourceId) {
        return ResponseEntity.ok(sourceMapper.toTextDto(sourceQueryService.getOwned(sourceId, currentUser.id())));
    }

    /**
     * Удаление источника.
     *
     * @param sourceId идентификатор источника
     * @return 204
     */
    @Override
    public ResponseEntity<Void> deleteSource(UUID sourceId) {
        sourceDeletionService.delete(sourceId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
