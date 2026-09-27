package net.pamytno.topic.repository;

import jakarta.persistence.LockModeType;
import net.pamytno.topic.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище тем.
 */
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    /**
     * Темы пользователя, новые сверху.
     *
     * @param userId владелец
     * @return темы пользователя
     */
    List<Topic> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Тема пользователя по идентификатору.
     *
     * @param id     идентификатор темы
     * @param userId владелец
     * @return тема, если она есть и принадлежит пользователю
     */
    Optional<Topic> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Тема с блокировкой строки — чтобы параллельная обработка источников не собрала две версии текста.
     *
     * @param id идентификатор темы
     * @return тема, если есть
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Topic t where t.id = :id")
    Optional<Topic> findByIdForUpdate(UUID id);
}
