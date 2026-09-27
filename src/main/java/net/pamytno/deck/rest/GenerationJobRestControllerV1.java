package net.pamytno.deck.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.deck.mapper.GenerationJobMapper;
import net.pamytno.deck.rest.api.GenerationJobsApi;
import net.pamytno.deck.rest.dto.GenerationJobDto;
import net.pamytno.deck.service.GenerationJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Статус задач генерации ({@code /api/generation-jobs/{jobId}}).
 */
@RestController
@RequiredArgsConstructor
public class GenerationJobRestControllerV1 implements GenerationJobsApi {

    private final GenerationJobService jobService;
    private final GenerationJobMapper jobMapper;
    private final CurrentUser currentUser;

    /**
     * Задача по идентификатору — для опроса статуса.
     *
     * @param jobId идентификатор задачи
     * @return 200 и задача
     */
    @Override
    public ResponseEntity<GenerationJobDto> getGenerationJob(UUID jobId) {
        return ResponseEntity.ok(jobMapper.toDto(jobService.getOwned(jobId, currentUser.id())));
    }
}
