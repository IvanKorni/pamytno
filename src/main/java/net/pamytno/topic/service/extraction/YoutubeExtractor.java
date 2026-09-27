package net.pamytno.topic.service.extraction;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.YoutubeVideoId;
import net.pamytno.topic.integration.youtube.YoutubeTranscriptClient;
import net.pamytno.topic.integration.youtube.YoutubeTranscriptException;
import org.springframework.stereotype.Component;

/**
 * Получает текст видео YouTube из доступных субтитров.
 */
@Component
@RequiredArgsConstructor
public class YoutubeExtractor implements SourceTextExtractor {

    private final YoutubeTranscriptClient transcriptClient;

    /**
     * Поддерживает {@link SourceType#YOUTUBE}.
     *
     * @param type вид источника
     * @return {@code true} для YouTube
     */
    @Override
    public boolean supports(SourceType type) {
        return type == SourceType.YOUTUBE;
    }

    /**
     * Загружает субтитры видео.
     *
     * @param source источник YouTube
     * @return текст субтитров
     * @throws TextExtractionException с кодом {@code TRANSCRIPT_UNAVAILABLE}, если субтитров нет
     */
    @Override
    public String extract(Source source) {
        var videoId = YoutubeVideoId.fromUrl(source.getOriginalUrl())
                .orElseThrow(() -> unavailable("Ссылка не ведёт на видео YouTube", null));
        try {
            return transcriptClient.fetchTranscript(videoId.value());
        } catch (YoutubeTranscriptException e) {
            throw unavailable(e.getMessage(), e);
        }
    }

    /**
     * Ошибка «субтитры недоступны».
     *
     * @param message описание
     * @param cause   исходная ошибка или {@code null}
     * @return исключение извлечения
     */
    private static TextExtractionException unavailable(String message, Throwable cause) {
        return new TextExtractionException(SourceErrorCode.TRANSCRIPT_UNAVAILABLE, message, cause);
    }
}
