package net.pamytno.deck.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.deck.mapper.GenerationJobMapper;
import net.pamytno.deck.rest.api.VocabularyGenerationApi;
import net.pamytno.deck.rest.dto.GenerateVocabularyRequest;
import net.pamytno.deck.rest.dto.GenerationJobDto;
import net.pamytno.deck.service.VocabularyGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Запуск составления карточек слов ({@code /api/topics/{topicId}/vocabulary/generate}).
 */
@RestController
@RequiredArgsConstructor
public class VocabularyGenerationRestControllerV1 implements VocabularyGenerationApi {

    private final VocabularyGenerationService generationService;
    private final GenerationJobMapper jobMapper;
    private final CurrentUser currentUser;

    /**
     * Запускает составление карточек слов по тексту.
     *
     * @param topicId идентификатор темы
     * @param request текст и инструкция, что из него взять
     * @return 202 и задача в статусе PROCESSING
     */
    @Override
    public ResponseEntity<GenerationJobDto> generateVocabulary(UUID topicId, GenerateVocabularyRequest request) {
        var job = generationService.start(currentUser.id(), topicId, request.getInstruction(), request.getText());
        return ResponseEntity.accepted().body(jobMapper.toDto(job));
    }
}
