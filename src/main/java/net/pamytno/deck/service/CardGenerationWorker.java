package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;

/**
 * Выполняет задачу генерации карточек: для каждого одобренного вопроса просит модель составить
 * карточку по цитате и фрагменту. Работает вне транзакции — вызовы AI долгие.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardGenerationWorker {

    private final QuestionQueryService questionQueryService;
    private final CardContextBuilder contextBuilder;
    private final AiProvider aiProvider;
    private final FlashcardWriter flashcardWriter;
    private final GenerationJobService jobService;
    private final Clock clock;

    /**
     * Генерирует карточки и завершает задачу. Созданное до ошибки сохраняется.
     *
     * @param request запрос на генерацию
     */
    public void generate(CardGenerationRequested request) {
        var startedAt = clock.instant();
        var questions = questionQueryService.list(request.topicId(), request.userId(), QuestionStatus.APPROVED);
        log.info("Генерация карточек [{}] для темы [{}] запущена: [{}] вопросов",
                request.jobId(), request.topicId(), questions.size());
        var created = 0;
        try {
            for (var question : questions) {
                created += createCard(question);
            }
            jobService.complete(request.jobId(), created);
            log.info("Генерация карточек [{}] завершена: [{}] карточек за [{}] мс", request.jobId(), created,
                    Duration.between(startedAt, clock.instant()).toMillis());
        } catch (AiGenerationException e) {
            log.warn("Генерация карточек [{}] прервана ошибкой AI: {}", request.jobId(), e.getMessage());
            jobService.fail(request.jobId(), created, "AI не смог составить карточки: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Непредвиденная ошибка генерации карточек [{}]", request.jobId(), e);
            jobService.fail(request.jobId(), created, "Не удалось составить карточки");
        }
    }

    /**
     * Просит модель составить карточку по вопросу и сохраняет её.
     *
     * @param question одобренный вопрос
     * @return 1, если карточка создана, иначе 0
     */
    private int createCard(Question question) {
        var card = aiProvider.generateCard(question.getText(), contextBuilder.contextFor(question));
        return flashcardWriter.create(question.getId(), card) ? 1 : 0;
    }
}
