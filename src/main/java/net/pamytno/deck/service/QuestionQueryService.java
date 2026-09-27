package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.exception.QuestionNotFoundException;
import net.pamytno.deck.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Чтение вопросов пользователя.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionQueryService {

    private final QuestionRepository questionRepository;

    /**
     * Вопросы темы по порядку появления.
     *
     * @param topicId идентификатор темы
     * @param userId  владелец
     * @param status  фильтр по статусу или {@code null}
     * @return вопросы темы
     */
    public List<Question> list(UUID topicId, UUID userId, QuestionStatus status) {
        return status == null
                ? questionRepository.findAllByTopicIdAndUserIdOrderBySeqAsc(topicId, userId)
                : questionRepository.findAllByTopicIdAndUserIdAndStatusOrderBySeqAsc(topicId, userId, status);
    }

    /**
     * Вопрос пользователя.
     *
     * @param questionId идентификатор вопроса
     * @param userId     владелец
     * @return вопрос
     * @throws QuestionNotFoundException если вопроса нет или он чужой
     */
    public Question getOwned(UUID questionId, UUID userId) {
        return questionRepository.findByIdAndUserId(questionId, userId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
    }
}
