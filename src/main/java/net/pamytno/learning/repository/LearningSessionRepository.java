package net.pamytno.learning.repository;

import net.pamytno.learning.domain.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

    /**
     * Удаляет все сессии темы.
     *
     * @param topicId тема
     * @return сколько удалено
     */
    @Modifying
    @Query("delete from LearningSession s where s.topicId = :topicId")
    int deleteAllByTopic(UUID topicId);
}
