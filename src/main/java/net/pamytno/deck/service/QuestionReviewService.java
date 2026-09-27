package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionDecision;
import net.pamytno.deck.exception.QuestionNotFoundException;
import net.pamytno.deck.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Отбор вопросов пользователем: изучать или нет, поштучно и массово, и правка формулировки.
 */
@Service
@RequiredArgsConstructor
public class QuestionReviewService {

    private final QuestionQueryService questionQueryService;
    private final QuestionRepository questionRepository;
    private final Clock clock;

    /**
     * Применяет решение к одному вопросу.
     *
     * @param questionId идентификатор вопроса
     * @param userId     владелец
     * @param decision   решение
     * @return изменённый вопрос
     */
    @Transactional
    public Question decide(UUID questionId, UUID userId, QuestionDecision decision) {
        var question = questionQueryService.getOwned(questionId, userId);
        decision.applyTo(question, clock.instant());
        return question;
    }

    /**
     * Применяет решение ко всем вопросам списка — всё или ничего.
     *
     * @param questionIds идентификаторы вопросов
     * @param userId      владелец
     * @param decision    решение
     * @return изменённые вопросы в порядке генерации
     * @throws QuestionNotFoundException если хотя бы один вопрос не найден или чужой
     */
    @Transactional
    public List<Question> decideAll(List<UUID> questionIds, UUID userId, QuestionDecision decision) {
        var questions = questionRepository.findAllByIdInAndUserId(questionIds, userId);
        var missing = new HashSet<>(questionIds);
        questions.forEach(question -> missing.remove(question.getId()));
        missing.stream().findFirst().ifPresent(id -> {
            throw new QuestionNotFoundException(id);
        });
        var now = clock.instant();
        questions.forEach(question -> decision.applyTo(question, now));
        return questions.stream()
                .sorted(Comparator.comparing(Question::getSeq, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * Меняет формулировку вопроса.
     *
     * @param questionId идентификатор вопроса
     * @param userId     владелец
     * @param text       новый текст
     * @return изменённый вопрос
     */
    @Transactional
    public Question rephrase(UUID questionId, UUID userId, String text) {
        var question = questionQueryService.getOwned(questionId, userId);
        question.rephrase(text.strip(), clock.instant());
        return question;
    }
}
