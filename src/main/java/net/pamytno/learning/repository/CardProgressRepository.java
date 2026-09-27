package net.pamytno.learning.repository;

import net.pamytno.learning.domain.CardProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
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
}
