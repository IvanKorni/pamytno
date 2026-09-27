package net.pamytno.deck.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.deck.mapper.GenerationJobMapper;
import net.pamytno.deck.rest.api.CardGenerationApi;
import net.pamytno.deck.rest.dto.GenerationJobDto;
import net.pamytno.deck.service.CardGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Запуск генерации карточек ({@code /api/topics/{topicId}/cards/generate}).
 */
@RestController
@RequiredArgsConstructor
public class CardGenerationRestControllerV1 implements CardGenerationApi {

    private final CardGenerationService generationService;
    private final GenerationJobMapper jobMapper;
    private final CurrentUser currentUser;

    /**
     * Запускает генерацию карточек по одобренным вопросам.
     *
     * @param topicId идентификатор темы
     * @return 202 и задача в статусе PROCESSING
     */
    @Override
    public ResponseEntity<GenerationJobDto> generateCards(UUID topicId) {
        return ResponseEntity.accepted().body(jobMapper.toDto(generationService.start(currentUser.id(), topicId)));
    }
}
