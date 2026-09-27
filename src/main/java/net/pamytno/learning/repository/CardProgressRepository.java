package net.pamytno.learning.repository;

import net.pamytno.learning.domain.CardProgress;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
