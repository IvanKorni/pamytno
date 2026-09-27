package net.pamytno.topic.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.topic.mapper.SourceMapper;
import net.pamytno.topic.rest.api.YoutubeSourcesApi;
import net.pamytno.topic.rest.dto.SourceDto;
import net.pamytno.topic.rest.dto.YoutubeSourceRequest;
import net.pamytno.topic.service.YoutubeSourceSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Добавление видео YouTube ({@code /api/topics/{topicId}/sources/youtube}).
 */
@RestController
@RequiredArgsConstructor
public class YoutubeSourceRestControllerV1 implements YoutubeSourcesApi {

    private final YoutubeSourceSubmissionService submissionService;
    private final SourceMapper sourceMapper;
    private final CurrentUser currentUser;

    /**
     * Принимает видео в обработку.
     *
     * @param topicId идентификатор темы
     * @param request ссылка и название
     * @return 202 и источник в статусе UPLOADED
     */
    @Override
    public ResponseEntity<SourceDto> addYoutubeSource(UUID topicId, YoutubeSourceRequest request) {
        var source = submissionService.submit(currentUser.id(), topicId, request.getUrl(), request.getName());
        return ResponseEntity.accepted().body(sourceMapper.toDto(source));
    }
}
