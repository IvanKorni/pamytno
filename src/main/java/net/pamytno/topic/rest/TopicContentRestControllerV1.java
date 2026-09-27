package net.pamytno.topic.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.topic.mapper.TopicContentMapper;
import net.pamytno.topic.rest.api.TopicContentApi;
import net.pamytno.topic.rest.dto.TopicContentDto;
import net.pamytno.topic.service.TopicContentQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Единый текст темы ({@code /api/topics/{topicId}/content}).
 */
@RestController
@RequiredArgsConstructor
public class TopicContentRestControllerV1 implements TopicContentApi {

    private final TopicContentQueryService contentQueryService;
    private final TopicContentMapper contentMapper;
    private final CurrentUser currentUser;

    /**
     * Последняя версия единого текста.
     *
     * @param topicId идентификатор темы
     * @return 200 и текст темы
     */
    @Override
    public ResponseEntity<TopicContentDto> getTopicContent(UUID topicId) {
        return ResponseEntity.ok(contentMapper.toDto(contentQueryService.getLatest(topicId, currentUser.id())));
    }
}
