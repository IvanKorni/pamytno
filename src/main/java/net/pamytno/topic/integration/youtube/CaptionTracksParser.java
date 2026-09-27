package net.pamytno.topic.integration.youtube;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Достаёт дорожки субтитров из ответа innertube {@code /youtubei/v1/player}.
 */
public final class CaptionTracksParser {

    private static final String PLAYABLE = "OK";

    /**
     * Запрещает создание экземпляров.
     */
    private CaptionTracksParser() {
    }

    /**
     * Разбирает ответ плеера.
     *
     * @param player JSON ответа плеера
     * @return дорожки субтитров
     * @throws YoutubeTranscriptException если видео недоступно или субтитров нет
     */
    public static List<CaptionTrack> parse(JsonNode player) {
        var status = player.path("playabilityStatus");
        if (!PLAYABLE.equals(status.path("status").asText(PLAYABLE))) {
            throw new YoutubeTranscriptException("Видео недоступно: " + status.path("reason").asText("без причины"));
        }
        var tracks = player.path("captions").path("playerCaptionsTracklistRenderer").path("captionTracks");
        if (!tracks.isArray() || tracks.isEmpty()) {
            throw new YoutubeTranscriptException("У видео нет доступных субтитров");
        }
        return StreamSupport.stream(tracks.spliterator(), false).map(CaptionTracksParser::toTrack).toList();
    }

    /**
     * Преобразует JSON дорожки.
     *
     * @param node JSON дорожки
     * @return дорожка
     */
    private static CaptionTrack toTrack(JsonNode node) {
        return new CaptionTrack(node.path("languageCode").asText(), node.path("baseUrl").asText(),
                "asr".equals(node.path("kind").asText()));
    }
}
