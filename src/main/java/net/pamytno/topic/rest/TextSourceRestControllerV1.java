package net.pamytno.topic.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.topic.mapper.SourceMapper;
import net.pamytno.topic.rest.api.TextSourcesApi;
import net.pamytno.topic.rest.dto.SourceDto;
import net.pamytno.topic.rest.dto.TextSourceRequest;
import net.pamytno.topic.service.SourceSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Добавление текста и списка слов ({@code /api/topics/{topicId}/sources/text}).
 */
@RestController
@RequiredArgsConstructor
public class TextSourceRestControllerV1 implements TextSourcesApi {

    private final SourceSubmissionService submissionService;
    private final SourceMapper sourceMapper;
    private final CurrentUser currentUser;

    /**
     * Принимает текст в обработку.
     *
     * @param topicId идентификатор темы
     * @param request вид, название и текст
     * @return 202 и источник в статусе UPLOADED
     */
    @Override
    public ResponseEntity<SourceDto> addTextSource(UUID topicId, TextSourceRequest request) {
        var source = submissionService.submitText(currentUser.id(), topicId,
                sourceMapper.toSourceType(request.getType()), request.getName(), request.getText());
        return ResponseEntity.accepted().body(sourceMapper.toDto(source));
    }
}
