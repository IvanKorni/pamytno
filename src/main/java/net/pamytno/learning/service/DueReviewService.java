package net.pamytno.learning.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.repository.CardProgressRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Очередь повторения: карточки темы, срок которых наступил.
 */
@Service
@RequiredArgsConstructor
public class DueReviewService {

    private final CardProgressRepository progressRepository;
    private final Clock clock;

    /**
     * Карточки к повторению, самые давние первыми.
     *
     * @param topicId тема
     * @param userId  пользователь
     * @param limit   сколько карточек вернуть
     * @return карточки к повторению
     */
    @Transactional(readOnly = true)
    public List<CardProgress> due(UUID topicId, UUID userId, int limit) {
        return progressRepository.findDue(userId, topicId, clock.instant(), Limit.of(limit));
    }
}
