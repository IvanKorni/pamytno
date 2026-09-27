package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.deck.integration.ai.AiGenerationException;
import net.pamytno.deck.integration.ai.AiProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;

/**
 * Выполняет задачу генерации вопросов: по каждому фрагменту актуальной версии текста
 * спрашивает модель и сохраняет ответ. Работает вне транзакции — вызовы AI долгие.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerationWorker {

    private final TopicMaterialService topicMaterialService;
    private final AiProvider aiProvider;
    private final QuestionWriter questionWriter;
    private final GenerationJobService jobService;
    private final Clock clock;

    /**
     * Генерирует вопросы и завершает задачу. Созданное до ошибки сохраняется.
     *
     * @param request запрос на генерацию
     */
    public void generate(QuestionGenerationRequested request) {
        var startedAt = clock.instant();
        var chunks = topicMaterialService.latestChunks(request.topicId(), request.userId());
        log.info("Генерация вопросов [{}] для темы [{}] запущена: [{}] фрагментов",
                request.jobId(), request.topicId(), chunks.size());
        var created = 0;
        try {
            for (var chunk : chunks) {
                created += questionWriter.save(chunk, aiProvider.generateQuestions(chunk.getContent()));
            }
            jobService.complete(request.jobId(), created);
            log.info("Генерация вопросов [{}] завершена: [{}] вопросов за [{}] мс", request.jobId(), created,
                    Duration.between(startedAt, clock.instant()).toMillis());
        } catch (AiGenerationException e) {
            log.warn("Генерация вопросов [{}] прервана ошибкой AI: {}", request.jobId(), e.getMessage());
            jobService.fail(request.jobId(), created, "AI не смог сгенерировать вопросы: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Непредвиденная ошибка генерации вопросов [{}]", request.jobId(), e);
            jobService.fail(request.jobId(), created, "Не удалось сгенерировать вопросы");
        }
    }
}
