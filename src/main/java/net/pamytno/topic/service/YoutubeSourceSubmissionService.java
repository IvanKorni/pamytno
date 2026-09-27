package net.pamytno.topic.service;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.YoutubeVideoId;
import net.pamytno.topic.exception.InvalidYoutubeUrlException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Приём видео YouTube: проверка ссылки и регистрация источника.
 */
@Service
@RequiredArgsConstructor
public class YoutubeSourceSubmissionService {

    private final TopicQueryService topicQueryService;
    private final SourceRegistrar sourceRegistrar;
    private final Clock clock;

    /**
     * Добавляет видео в тему.
     *
     * @param userId  владелец темы
     * @param topicId идентификатор темы
     * @param url     ссылка на видео
     * @param name    название, может быть {@code null}
     * @return источник в статусе UPLOADED
     * @throws InvalidYoutubeUrlException если ссылка не ведёт на видео YouTube
     */
    @Transactional
    public Source submit(UUID userId, UUID topicId, String url, String name) {
        if (YoutubeVideoId.fromUrl(url).isEmpty()) {
            throw new InvalidYoutubeUrlException();
        }
        var topic = topicQueryService.getOwned(topicId, userId);
        return sourceRegistrar.register(topic, Source.youtube(topic, url.strip(), name, clock.instant()));
    }
}
