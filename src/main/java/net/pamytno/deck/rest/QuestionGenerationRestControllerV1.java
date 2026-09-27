package net.pamytno.deck.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.deck.mapper.GenerationJobMapper;
import net.pamytno.deck.rest.api.QuestionGenerationApi;
import net.pamytno.deck.rest.dto.GenerationJobDto;
import net.pamytno.deck.service.QuestionGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Запуск генерации вопросов ({@code /api/topics/{topicId}/questions/generate}).
 */
@RestController
@RequiredArgsConstructor
public class QuestionGenerationRestControllerV1 implements QuestionGenerationApi {

    private final QuestionGenerationService generationService;
    private final GenerationJobMapper jobMapper;
    private final CurrentUser currentUser;

    /**
     * Запускает генерацию вопросов.
     *
     * @param topicId идентификатор темы
     * @return 202 и задача в статусе PROCESSING
     */
    @Override
    public ResponseEntity<GenerationJobDto> generateQuestions(UUID topicId) {
        return ResponseEntity.accepted().body(jobMapper.toDto(generationService.start(currentUser.id(), topicId)));
    }
}
