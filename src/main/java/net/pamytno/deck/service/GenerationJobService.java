package net.pamytno.deck.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.domain.GenerationJobStatus;
import net.pamytno.deck.domain.GenerationJobType;
import net.pamytno.deck.domain.TopicRef;
import net.pamytno.deck.exception.GenerationInProgressException;
import net.pamytno.deck.exception.GenerationJobNotFoundException;
import net.pamytno.deck.repository.GenerationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Жизненный цикл задач генерации: запуск (не больше одной задачи вида на тему), завершение, чтение.
 */
@Service
@RequiredArgsConstructor
public class GenerationJobService {

    private final GenerationJobRepository jobRepository;
    private final Clock clock;

    /**
     * Запускает задачу в транзакции вызывающего сервиса.
     *
     * @param topic тема и владелец
     * @param type  вид генерации
     * @return задача в статусе PROCESSING
     * @throws GenerationInProgressException если такая генерация по теме уже идёт
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public GenerationJob start(TopicRef topic, GenerationJobType type) {
        if (jobRepository.existsByTopicIdAndTypeAndStatus(topic.topicId(), type, GenerationJobStatus.PROCESSING)) {
            throw new GenerationInProgressException();
        }
        return jobRepository.save(new GenerationJob(topic, type, clock.instant()));
    }

    /**
     * Завершает задачу успешно.
     *
     * @param jobId   идентификатор задачи
     * @param created сколько элементов создано
     */
    @Transactional
    public void complete(UUID jobId, int created) {
        jobRepository.findById(jobId).ifPresent(job -> job.complete(created, clock.instant()));
    }

    /**
     * Завершает задачу ошибкой.
     *
     * @param jobId   идентификатор задачи
     * @param created сколько элементов успели создать
     * @param message описание ошибки
     */
    @Transactional
    public void fail(UUID jobId, int created, String message) {
        jobRepository.findById(jobId).ifPresent(job -> job.fail(created, message, clock.instant()));
    }

    /**
     * Задача пользователя.
     *
     * @param jobId  идентификатор задачи
     * @param userId владелец
     * @return задача
     * @throws GenerationJobNotFoundException если задачи нет или она чужая
     */
    @Transactional(readOnly = true)
    public GenerationJob getOwned(UUID jobId, UUID userId) {
        return jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new GenerationJobNotFoundException(jobId));
    }
}
