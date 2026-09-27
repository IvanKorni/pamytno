package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.exception.UnsupportedFileTypeException;
import net.pamytno.topic.integration.storage.LocalFileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Arrays;
import java.util.UUID;

/**
 * Приём PDF: проверка сигнатуры, сохранение файла в хранилище и регистрация источника.
 */
@Service
@RequiredArgsConstructor
public class PdfSourceSubmissionService {

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final TopicQueryService topicQueryService;
    private final SourceRegistrar sourceRegistrar;
    private final LocalFileStorage storage;
    private final Clock clock;

    /**
     * Сохраняет PDF и отправляет его на обработку.
     *
     * @param userId   владелец темы
     * @param topicId  идентификатор темы
     * @param fileName имя файла или название, заданное пользователем
     * @param content  содержимое файла
     * @return источник в статусе UPLOADED
     * @throws UnsupportedFileTypeException если файл не PDF
     */
    @Transactional
    public Source submit(UUID userId, UUID topicId, String fileName, byte[] content) {
        requirePdf(content);
        var topic = topicQueryService.getOwned(topicId, userId);
        var source = Source.pdf(topic, fileName, clock.instant());
        storage.save(source.getStorageKey(), content);
        return sourceRegistrar.register(topic, source);
    }

    /**
     * Проверяет сигнатуру PDF в начале файла.
     *
     * @param content содержимое файла
     */
    private static void requirePdf(byte[] content) {
        var isPdf = content.length >= PDF_SIGNATURE.length
                && Arrays.equals(content, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length);
        if (!isPdf) {
            throw new UnsupportedFileTypeException();
        }
    }
}
