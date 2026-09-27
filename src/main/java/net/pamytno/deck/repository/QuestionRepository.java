package net.pamytno.deck.repository;

import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Хранилище вопросов.
 */
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    /**
     * Все вопросы темы пользователя по порядку появления.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @return вопросы темы
     */
    List<Question> findAllByTopicIdAndUserIdOrderByCreatedAtAscIdAsc(UUID topicId, UUID userId);

    /**
     * Вопросы темы пользователя в статусе по порядку появления.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @param status  статус
     * @return вопросы темы в статусе
     */
    List<Question> findAllByTopicIdAndUserIdAndStatusOrderByCreatedAtAscIdAsc(UUID topicId, UUID userId,
                                                                              QuestionStatus status);
}
