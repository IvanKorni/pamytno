package net.pamytno.deck.repository;

import net.pamytno.deck.domain.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище карточек.
 */
public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

    /**
     * Карточки темы пользователя в порядке создания.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return карточки темы
     */
    List<Flashcard> findAllByTopicIdAndUserIdOrderBySeqAsc(UUID topicId, UUID userId);

    /**
     * Карточка пользователя.
     *
     * @param id     идентификатор карточки
     * @param userId владелец
     * @return карточка, если есть и принадлежит пользователю
     */
    Optional<Flashcard> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Удаляет все карточки темы.
     *
     * @param topicId идентификатор темы
     * @return сколько записей удалено
     */
    @Modifying
    @Query("delete from Flashcard f where f.topicId = :topicId")
    int deleteAllByTopic(UUID topicId);
}
