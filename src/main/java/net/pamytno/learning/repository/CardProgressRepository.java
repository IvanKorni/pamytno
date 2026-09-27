package net.pamytno.learning.repository;

import net.pamytno.learning.domain.CardProgress;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище прогресса карточек.
 */
public interface CardProgressRepository extends JpaRepository<CardProgress, UUID> {

    /**
     * Прогресс карточки.
     *
     * @param cardId идентификатор карточки
     * @return прогресс, если карточка известна модулю
     */
    Optional<CardProgress> findByCardId(UUID cardId);

    /**
     * Удаляет прогресс карточки.
     *
     * @param cardId идентификатор карточки
     */
    void deleteByCardId(UUID cardId);

    /**
     * Сколько карточек темы пора повторить.
     *
     * @param userId  пользователь
     * @param topicId тема
     * @param now     текущий момент
     * @return число карточек со сроком повторения не позже {@code now}
     */
    long countByUserIdAndTopicIdAndNextReviewAtLessThanEqual(UUID userId, UUID topicId, Instant now);

    /**
     * Прогресс карточки пользователя.
     *
     * @param cardId идентификатор карточки
     * @param userId владелец
     * @return прогресс, если карточка известна и принадлежит пользователю
     */
    Optional<CardProgress> findByCardIdAndUserId(UUID cardId, UUID userId);

    /**
     * Карточки темы, срок повторения которых наступил, — самые давние первыми.
     *
     * @param userId  пользователь
     * @param topicId тема
     * @param now     текущий момент
     * @param limit   сколько карточек вернуть
     * @return карточки к повторению
     */
    @Query("""
            select p from CardProgress p
            where p.userId = :userId and p.topicId = :topicId and p.nextReviewAt <= :now
            order by p.nextReviewAt asc, p.createdAt asc""")
    List<CardProgress> findDue(UUID userId, UUID topicId, Instant now, Limit limit);
}
