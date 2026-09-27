package net.pamytno.learning.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.learning.mapper.ReviewMapper;
import net.pamytno.learning.rest.api.ReviewsApi;
import net.pamytno.learning.rest.dto.DueCardDto;
import net.pamytno.learning.rest.dto.ReviewRequest;
import net.pamytno.learning.rest.dto.ReviewResultDto;
import net.pamytno.learning.service.DueReviewService;
import net.pamytno.learning.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Повторение карточек ({@code /api/topics/{topicId}/reviews/due}, {@code /api/cards/{cardId}/review}).
 */
@RestController
@RequiredArgsConstructor
public class ReviewRestControllerV1 implements ReviewsApi {

    private final DueReviewService dueReviewService;
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final CurrentUser currentUser;

    /**
     * Карточки темы к повторению.
     *
     * @param topicId идентификатор темы
     * @param limit   сколько карточек вернуть
     * @return 200 и карточки
     */
    @Override
    public ResponseEntity<List<DueCardDto>> getDueReviews(UUID topicId, Integer limit) {
        return ResponseEntity.ok(reviewMapper.toDueCards(dueReviewService.due(topicId, currentUser.id(), limit)));
    }

    /**
     * Ответ по карточке.
     *
     * @param cardId  идентификатор карточки
     * @param request ответ и сессия
     * @return 200 и новое состояние карточки
     */
    @Override
    public ResponseEntity<ReviewResultDto> reviewCard(UUID cardId, ReviewRequest request) {
        var outcome = reviewService.review(cardId, currentUser.id(), reviewMapper.toResult(request.getResult()),
                request.getSessionId());
        return ResponseEntity.ok(reviewMapper.toDto(outcome));
    }
}
