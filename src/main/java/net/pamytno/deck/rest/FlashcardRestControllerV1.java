package net.pamytno.deck.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.deck.mapper.FlashcardMapper;
import net.pamytno.deck.rest.api.CardsApi;
import net.pamytno.deck.rest.dto.FlashcardDto;
import net.pamytno.deck.rest.dto.UpdateCardRequest;
import net.pamytno.deck.service.FlashcardCommandService;
import net.pamytno.deck.service.FlashcardQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Карточки: просмотр, редактирование, удаление ({@code /api/topics/{topicId}/cards}, {@code /api/cards}).
 */
@RestController
@RequiredArgsConstructor
public class FlashcardRestControllerV1 implements CardsApi {

    private final FlashcardQueryService flashcardQueryService;
    private final FlashcardCommandService flashcardCommandService;
    private final FlashcardMapper flashcardMapper;
    private final CurrentUser currentUser;

    /**
     * Все карточки темы.
     *
     * @param topicId идентификатор темы
     * @return 200 и карточки
     */
    @Override
    public ResponseEntity<List<FlashcardDto>> listCards(UUID topicId) {
        return ResponseEntity.ok(flashcardMapper.toDtos(flashcardQueryService.list(topicId, currentUser.id())));
    }

    /**
     * Карточка по идентификатору.
     *
     * @param cardId идентификатор карточки
     * @return 200 и карточка
     */
    @Override
    public ResponseEntity<FlashcardDto> getCard(UUID cardId) {
        return ResponseEntity.ok(flashcardMapper.toDto(flashcardQueryService.getOwned(cardId, currentUser.id())));
    }

    /**
     * Частичное изменение карточки.
     *
     * @param cardId  идентификатор карточки
     * @param request новые стороны карточки
     * @return 200 и карточка
     */
    @Override
    public ResponseEntity<FlashcardDto> updateCard(UUID cardId, UpdateCardRequest request) {
        var card = flashcardCommandService.update(cardId, currentUser.id(), request.getFront(), request.getBack());
        return ResponseEntity.ok(flashcardMapper.toDto(card));
    }

    /**
     * Удаление карточки.
     *
     * @param cardId идентификатор карточки
     * @return 204
     */
    @Override
    public ResponseEntity<Void> deleteCard(UUID cardId) {
        flashcardCommandService.delete(cardId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
