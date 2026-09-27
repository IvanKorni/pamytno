package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.repository.SourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

/**
 * Короткие транзакции смены состояния источника во время асинхронной обработки.
 * Если источник успели удалить, переходы молча пропускаются.
 */
@Service
@RequiredArgsConstructor
public class SourceStateService {

    private final SourceRepository sourceRepository;
    private final Clock clock;

    /**
     * Переводит источник в PROCESSING.
     *
     * @param sourceId идентификатор источника
     * @return источник, если он ещё существует
     */
    @Transactional
    public Optional<Source> startProcessing(UUID sourceId) {
        var source = sourceRepository.findById(sourceId);
        source.ifPresent(found -> found.startProcessing(clock.instant()));
        return source;
    }

    /**
     * Сохраняет извлечённый текст.
     *
     * @param sourceId идентификатор источника
     * @param text     очищенный текст
     */
    @Transactional
    public void complete(UUID sourceId, String text) {
        sourceRepository.findById(sourceId).ifPresent(source -> source.completeExtraction(text, clock.instant()));
    }

    /**
     * Фиксирует ошибку обработки.
     *
     * @param sourceId идентификатор источника
     * @param code     причина
     * @param message  описание
     */
    @Transactional
    public void fail(UUID sourceId, SourceErrorCode code, String message) {
        sourceRepository.findById(sourceId)
                .ifPresent(source -> source.failExtraction(code, message, clock.instant()));
    }
}
