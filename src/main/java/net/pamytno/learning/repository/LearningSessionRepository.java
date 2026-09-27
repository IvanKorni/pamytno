package net.pamytno.learning.repository;

import net.pamytno.learning.domain.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище учебных сессий.
 */
public interface LearningSessionRepository extends JpaRepository<LearningSession, UUID> {

    /**
     * Сессия пользователя.
     *
     * @param id     идентификатор сессии
     * @param userId пользователь
     * @return сессия, если есть и принадлежит пользователю
     */
    Optional<LearningSession> findByIdAndUserId(UUID id, UUID userId);
}
