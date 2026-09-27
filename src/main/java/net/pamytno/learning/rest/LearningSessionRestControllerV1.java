package net.pamytno.learning.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.learning.mapper.LearningSessionMapper;
import net.pamytno.learning.rest.api.LearningSessionsApi;
import net.pamytno.learning.rest.dto.LearningSessionDto;
import net.pamytno.learning.service.LearningSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Учебные сессии ({@code /api/topics/{topicId}/learning-sessions}, {@code /api/learning-sessions}).
 */
@RestController
@RequiredArgsConstructor
public class LearningSessionRestControllerV1 implements LearningSessionsApi {

    private final LearningSessionService sessionService;
    private final LearningSessionMapper sessionMapper;
    private final CurrentUser currentUser;

    /**
     * Начинает сессию.
     *
     * @param topicId идентификатор темы
     * @return 201 и сессия
     */
    @Override
    public ResponseEntity<LearningSessionDto> startLearningSession(UUID topicId) {
        var session = sessionService.start(currentUser.id(), topicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionMapper.toDto(session));
    }

    /**
     * Сессия по идентификатору.
     *
     * @param sessionId идентификатор сессии
     * @return 200 и сессия
     */
    @Override
    public ResponseEntity<LearningSessionDto> getLearningSession(UUID sessionId) {
        return ResponseEntity.ok(sessionMapper.toDto(sessionService.getOwned(sessionId, currentUser.id())));
    }

    /**
     * Завершает сессию.
     *
     * @param sessionId идентификатор сессии
     * @return 200 и завершённая сессия
     */
    @Override
    public ResponseEntity<LearningSessionDto> completeLearningSession(UUID sessionId) {
        return ResponseEntity.ok(sessionMapper.toDto(sessionService.complete(sessionId, currentUser.id())));
    }
}
