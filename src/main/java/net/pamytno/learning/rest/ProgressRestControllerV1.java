package net.pamytno.learning.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.learning.mapper.ProgressMapper;
import net.pamytno.learning.rest.api.ProgressApi;
import net.pamytno.learning.rest.dto.DashboardDto;
import net.pamytno.learning.rest.dto.TopicProgressDto;
import net.pamytno.learning.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Прогресс ({@code /api/topics/{topicId}/progress}, {@code /api/dashboard}).
 */
@RestController
@RequiredArgsConstructor
public class ProgressRestControllerV1 implements ProgressApi {

    private final ProgressService progressService;
    private final ProgressMapper progressMapper;
    private final CurrentUser currentUser;

    /**
     * Прогресс темы.
     *
     * @param topicId идентификатор темы
     * @return 200 и прогресс
     */
    @Override
    public ResponseEntity<TopicProgressDto> getTopicProgress(UUID topicId) {
        return ResponseEntity.ok(progressMapper.toDto(progressService.topic(topicId, currentUser.id())));
    }

    /**
     * Общий прогресс пользователя.
     *
     * @return 200 и dashboard
     */
    @Override
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(progressMapper.toDto(progressService.dashboard(currentUser.id())));
    }
}
