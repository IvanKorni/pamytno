package net.pamytno.topic.rest;

import lombok.RequiredArgsConstructor;
import net.pamytno.common.security.CurrentUser;
import net.pamytno.topic.mapper.TopicMapper;
import net.pamytno.topic.rest.api.TopicsApi;
import net.pamytno.topic.rest.dto.CreateTopicRequest;
import net.pamytno.topic.rest.dto.TopicDto;
import net.pamytno.topic.rest.dto.UpdateTopicRequest;
import net.pamytno.topic.service.TopicCommandService;
import net.pamytno.topic.service.TopicDeletionService;
import net.pamytno.topic.service.TopicQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * CRUD тем текущего пользователя ({@code /api/topics}).
 */
@RestController
@RequiredArgsConstructor
public class TopicRestControllerV1 implements TopicsApi {

    private final TopicQueryService topicQueryService;
    private final TopicCommandService topicCommandService;
    private final TopicDeletionService topicDeletionService;
    private final TopicMapper topicMapper;
    private final CurrentUser currentUser;

    /**
     * Список тем.
     *
     * @return 200 и темы пользователя
     */
    @Override
    public ResponseEntity<List<TopicDto>> listTopics() {
        return ResponseEntity.ok(topicMapper.toDtos(topicQueryService.list(currentUser.id())));
    }

    /**
     * Создание темы.
     *
     * @param request название и описание
     * @return 201 и созданная тема
     */
    @Override
    public ResponseEntity<TopicDto> createTopic(CreateTopicRequest request) {
        var topic = topicCommandService.create(currentUser.id(), request.getTitle(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(topicMapper.toDto(topic));
    }

    /**
     * Тема по идентификатору.
     *
     * @param topicId идентификатор темы
     * @return 200 и тема
     */
    @Override
    public ResponseEntity<TopicDto> getTopic(UUID topicId) {
        return ResponseEntity.ok(topicMapper.toDto(topicQueryService.getOwned(topicId, currentUser.id())));
    }

    /**
     * Частичное изменение темы.
     *
     * @param topicId идентификатор темы
     * @param request изменяемые поля
     * @return 200 и обновлённая тема
     */
    @Override
    public ResponseEntity<TopicDto> updateTopic(UUID topicId, UpdateTopicRequest request) {
        var topic = topicCommandService.update(topicId, currentUser.id(), request.getTitle(),
                request.getDescription());
        return ResponseEntity.ok(topicMapper.toDto(topic));
    }

    /**
     * Удаление темы.
     *
     * @param topicId идентификатор темы
     * @return 204
     */
    @Override
    public ResponseEntity<Void> deleteTopic(UUID topicId) {
        topicDeletionService.delete(topicId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
