package net.pamytno.learning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.domain.ReviewResult;
import net.pamytno.learning.exception.CardProgressNotFoundException;
import net.pamytno.learning.exception.LearningSessionNotFoundException;
import net.pamytno.learning.exception.LearningSessionTopicMismatchException;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.LearningSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Принимает ответ пользователя по карточке: двигает карточку по расписанию и учитывает ответ в сессии.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CardProgressRepository progressRepository;
    private final LearningSessionRepository sessionRepository;
    private final Clock clock;

    /**
     * Применяет ответ.
     *
     * @param cardId    идентификатор карточки
     * @param userId    пользователь
     * @param result    ответ
     * @param sessionId текущая сессия или {@code null}
     * @return новое состояние карточки
     * @throws CardProgressNotFoundException если карточки нет в повторении пользователя
     */
    @Transactional
    public ReviewOutcome review(UUID cardId, UUID userId, ReviewResult result, UUID sessionId) {
        var progress = progressRepository.findByCardIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardProgressNotFoundException(cardId));
        if (sessionId != null) {
            registerInSession(sessionId, userId, progress, result);
        }
        result.applyTo(progress, clock.instant());
        log.info("Карточка [{}]: ответ [{}], этап [{}], следующее повторение [{}]",
                cardId, result, progress.getStage(), progress.getNextReviewAt());
        return new ReviewOutcome(progress, result);
    }

    /**
     * Учитывает ответ в учебной сессии той же темы.
     *
     * @param sessionId идентификатор сессии
     * @param userId    пользователь
     * @param progress  прогресс карточки
     * @param result    ответ
     */
    private void registerInSession(UUID sessionId, UUID userId, CardProgress progress, ReviewResult result) {
        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new LearningSessionNotFoundException(sessionId));
        if (!session.getTopicId().equals(progress.getTopicId())) {
            throw new LearningSessionTopicMismatchException(sessionId);
        }
        session.register(result);
    }
}
