package net.pamytno.topic.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.topic.mapper.SourceMapper;
import net.pamytno.topic.rest.api.PdfSourcesApi;
import net.pamytno.topic.rest.dto.SourceDto;
import net.pamytno.topic.service.PdfSourceSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/**
 * Загрузка PDF ({@code /api/topics/{topicId}/sources/pdf}). Размер ограничен
 * {@code spring.servlet.multipart.max-file-size}.
 */
@RestController
@RequiredArgsConstructor
public class PdfSourceRestControllerV1 implements PdfSourcesApi {

    private final PdfSourceSubmissionService submissionService;
    private final SourceMapper sourceMapper;
    private final CurrentUser currentUser;

    /**
     * Принимает PDF в обработку.
     *
     * @param topicId идентификатор темы
     * @param file    PDF-файл
     * @param name    название источника; по умолчанию — имя файла
     * @return 202 и источник в статусе UPLOADED
     */
    @Override
    public ResponseEntity<SourceDto> addPdfSource(UUID topicId, MultipartFile file, String name) {
        var displayName = name == null || name.isBlank() ? file.getOriginalFilename() : name;
        var source = submissionService.submit(currentUser.id(), topicId, displayName, bytesOf(file));
        return ResponseEntity.accepted().body(sourceMapper.toDto(source));
    }

    /**
     * Читает содержимое загруженного файла.
     *
     * @param file файл из multipart-запроса
     * @return содержимое
     */
    private static byte[] bytesOf(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать загруженный файл", e);
        }
    }
}
