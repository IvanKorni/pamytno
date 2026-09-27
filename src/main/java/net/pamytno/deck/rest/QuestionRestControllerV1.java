package net.pamytno.deck.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.deck.domain.QuestionDecision;
import net.pamytno.deck.mapper.QuestionMapper;
import net.pamytno.deck.rest.api.QuestionsApi;
import net.pamytno.deck.rest.dto.QuestionDecisionRequest;
import net.pamytno.deck.rest.dto.QuestionDto;
import net.pamytno.deck.rest.dto.QuestionStatus;
import net.pamytno.deck.rest.dto.UpdateQuestionRequest;
import net.pamytno.deck.service.QuestionQueryService;
import net.pamytno.deck.service.QuestionReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Вопросы и их отбор пользователем ({@code /api/topics/{topicId}/questions}, {@code /api/questions}).
 */
@RestController
@RequiredArgsConstructor
public class QuestionRestControllerV1 implements QuestionsApi {

    private final QuestionQueryService questionQueryService;
    private final QuestionReviewService questionReviewService;
    private final QuestionMapper questionMapper;
    private final CurrentUser currentUser;

    /**
     * Вопросы темы с необязательным фильтром по статусу.
     *
     * @param topicId идентификатор темы
     * @param status  статус или {@code null}
     * @return 200 и вопросы
     */
    @Override
    public ResponseEntity<List<QuestionDto>> listQuestions(UUID topicId, QuestionStatus status) {
        var questions = questionQueryService.list(topicId, currentUser.id(), questionMapper.toStatus(status));
        return ResponseEntity.ok(questionMapper.toDtos(questions));
    }

    /**
     * Правка формулировки вопроса.
     *
     * @param questionId идентификатор вопроса
     * @param request    новый текст
     * @return 200 и вопрос
     */
    @Override
    public ResponseEntity<QuestionDto> updateQuestion(UUID questionId, UpdateQuestionRequest request) {
        var question = questionReviewService.rephrase(questionId, currentUser.id(), request.getText());
        return ResponseEntity.ok(questionMapper.toDto(question));
    }

    /**
     * Решение «изучать».
     *
     * @param questionId идентификатор вопроса
     * @return 200 и вопрос
     */
    @Override
    public ResponseEntity<QuestionDto> approveQuestion(UUID questionId) {
        var question = questionReviewService.decide(questionId, currentUser.id(), QuestionDecision.APPROVE);
        return ResponseEntity.ok(questionMapper.toDto(question));
    }

    /**
     * Решение «не изучать».
     *
     * @param questionId идентификатор вопроса
     * @return 200 и вопрос
     */
    @Override
    public ResponseEntity<QuestionDto> rejectQuestion(UUID questionId) {
        var question = questionReviewService.decide(questionId, currentUser.id(), QuestionDecision.REJECT);
        return ResponseEntity.ok(questionMapper.toDto(question));
    }

    /**
     * Массовое решение по вопросам.
     *
     * @param request вопросы и решение
     * @return 200 и изменённые вопросы
     */
    @Override
    public ResponseEntity<List<QuestionDto>> decideQuestions(QuestionDecisionRequest request) {
        var questions = questionReviewService.decideAll(request.getQuestionIds(), currentUser.id(),
                questionMapper.toDecision(request.getDecision()));
        return ResponseEntity.ok(questionMapper.toDtos(questions));
    }
}
