package net.pamytno.learning.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.learning.domain.LearningSession;
import net.pamytno.learning.exception.LearningSessionNotFoundException;
import net.pamytno.learning.repository.CardProgressRepository;
import net.pamytno.learning.repository.LearningSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Начало, чтение и завершение учебных сессий.
 */
@Service
@RequiredArgsConstructor
public class LearningSessionService {

    private final LearningSessionRepository sessionRepository;
    private final CardProgressRepository progressRepository;
    private final Clock clock;

    /**
     * Начинает сессию по теме; фиксирует, сколько карточек к повторению на старте.
     *
     * @param userId  пользователь
     * @param topicId тема
     * @return новая сессия
     */
    @Transactional
    public LearningSession start(UUID userId, UUID topicId) {
        var now = clock.instant();
        var due = progressRepository.countByUserIdAndTopicIdAndNextReviewAtLessThanEqual(userId, topicId, now);
        return sessionRepository.save(new LearningSession(userId, topicId, Math.toIntExact(due), now));
    }

    /**
     * Сессия пользователя.
     *
     * @param sessionId идентификатор сессии
     * @param userId    пользователь
     * @return сессия
     * @throws LearningSessionNotFoundException если сессии нет или она чужая
     */
    @Transactional(readOnly = true)
    public LearningSession getOwned(UUID sessionId, UUID userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new LearningSessionNotFoundException(sessionId));
    }

    /**
     * Завершает сессию.
     *
     * @param sessionId идентификатор сессии
     * @param userId    пользователь
     * @return завершённая сессия
     */
    @Transactional
    public LearningSession complete(UUID sessionId, UUID userId) {
        var session = getOwned(sessionId, userId);
        session.complete(clock.instant());
        return session;
    }
}
