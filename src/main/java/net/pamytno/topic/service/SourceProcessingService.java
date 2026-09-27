package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.service.extraction.SourceTextExtractors;
import net.pamytno.topic.service.extraction.TextExtractionException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Обрабатывает источник: извлекает текст вне транзакции (PDF и YouTube могут идти долго),
 * записывает результат и обновляет тему.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceProcessingService {

    private final SourceStateService sourceStateService;
    private final SourceTextExtractors extractors;
    private final TopicMaterialRefresher materialRefresher;
    private final Clock clock;

    /**
     * Обрабатывает источник, если он ещё существует.
     *
     * @param sourceId идентификатор источника
     */
    public void process(UUID sourceId) {
        sourceStateService.startProcessing(sourceId).ifPresent(this::extractAndRefresh);
    }

    /**
     * Извлекает текст, фиксирует результат и пересчитывает тему.
     *
     * @param source источник в статусе PROCESSING
     */
    private void extractAndRefresh(Source source) {
        var startedAt = clock.instant();
        try {
            sourceStateService.complete(source.getId(), extractors.extract(source));
            log.info("Источник [{}] обработан за [{}] мс", source.getId(), elapsedMillis(startedAt));
        } catch (TextExtractionException e) {
            log.warn("Источник [{}] не обработан: [{}] {}", source.getId(), e.getErrorCode(), e.getMessage());
            sourceStateService.fail(source.getId(), e.getErrorCode(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("Непредвиденная ошибка обработки источника [{}]", source.getId(), e);
            sourceStateService.fail(source.getId(), SourceErrorCode.SOURCE_PROCESSING_FAILED,
                    "Не удалось обработать источник");
        }
        materialRefresher.refresh(source.getTopicId());
    }

    /**
     * Сколько миллисекунд прошло с момента.
     *
     * @param startedAt момент начала
     * @return длительность в миллисекундах
     */
    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, clock.instant()).toMillis();
    }
}
