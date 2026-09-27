package net.pamytno.topic.integration.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import net.pamytno.topic.config.YoutubeProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Получает субтитры видео через внутреннее API YouTube (innertube), как это делают открытые библиотеки:
 * страница видео → ключ API → ответ плеера со списком дорожек → XML выбранной дорожки.
 * Собственного распознавания речи нет.
 */
@Slf4j
@Component
public class YoutubeTranscriptClient {

    private static final Pattern API_KEY = Pattern.compile("\"INNERTUBE_API_KEY\":\\s*\"([a-zA-Z0-9_-]+)\"");

    private final RestClient restClient;
    private final YoutubeProperties properties;

    /**
     * Создаёт клиент.
     *
     * @param restClient HTTP-клиент YouTube
     * @param properties настройки YouTube
     */
    public YoutubeTranscriptClient(@Qualifier("youtubeRestClient") RestClient restClient,
                                   YoutubeProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Загружает текст субтитров видео.
     *
     * @param videoId идентификатор видео
     * @return текст субтитров, реплика на строку
     * @throws YoutubeTranscriptException если субтитры получить не удалось
     */
    public String fetchTranscript(String videoId) {
        try {
            var tracks = CaptionTracksParser.parse(fetchPlayer(videoId, fetchApiKey(videoId)));
            var track = CaptionTrackSelector.select(tracks, properties.preferredLanguages())
                    .orElseThrow(() -> new YoutubeTranscriptException("У видео нет доступных субтитров"));
            log.info("Для видео [{}] выбраны субтитры [{}]", videoId, track.languageCode());
            return TranscriptXmlParser.parse(fetchTrackXml(track));
        } catch (RestClientException e) {
            log.warn("YouTube ответил ошибкой для видео [{}]: {}", videoId, e.getMessage());
            throw new YoutubeTranscriptException("YouTube не отдал субтитры видео", e);
        }
    }

    /**
     * Находит ключ innertube API на странице видео.
     *
     * @param videoId идентификатор видео
     * @return ключ API
     */
    private String fetchApiKey(String videoId) {
        var page = restClient.get().uri("/watch?v={id}", videoId).retrieve().body(String.class);
        var matcher = API_KEY.matcher(page == null ? "" : page);
        if (!matcher.find()) {
            throw new YoutubeTranscriptException("Не удалось открыть страницу видео");
        }
        return matcher.group(1);
    }

    /**
     * Запрашивает у плеера описание видео со списком субтитров.
     *
     * @param videoId идентификатор видео
     * @param apiKey  ключ innertube API
     * @return JSON ответа плеера
     */
    private JsonNode fetchPlayer(String videoId, String apiKey) {
        var client = Map.of("clientName", properties.clientName(), "clientVersion", properties.clientVersion());
        var body = Map.of("context", Map.of("client", client), "videoId", videoId);
        return restClient.post().uri("/youtubei/v1/player?key={key}", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Загружает XML дорожки в классическом формате (без {@code fmt=srv3}). Читается как UTF-8
     * независимо от заголовка: без charset Spring декодировал бы {@code text/xml} как ISO-8859-1.
     *
     * @param track дорожка
     * @return XML субтитров
     */
    private String fetchTrackXml(CaptionTrack track) {
        var url = track.baseUrl().replace("&fmt=srv3", "");
        var body = restClient.get().uri(URI.create(url)).retrieve().body(byte[].class);
        return body == null ? "" : new String(body, StandardCharsets.UTF_8);
    }
}
