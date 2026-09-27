package net.pamytno.topic;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.concurrent.ThreadLocalRandom;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Заглушки YouTube на WireMock: страница видео, ответ плеера и XML субтитров.
 *
 * @param server WireMock-сервер
 */
public record YoutubeStubs(WireMockServer server) {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    private static final int VIDEO_ID_LENGTH = 11;

    /**
     * Случайный идентификатор видео, чтобы заглушки разных тестов не пересекались.
     *
     * @return 11-символьный идентификатор
     */
    public static String randomVideoId() {
        var id = new StringBuilder();
        for (var i = 0; i < VIDEO_ID_LENGTH; i++) {
            id.append(ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length())));
        }
        return id.toString();
    }

    /**
     * Видео с одной ручной дорожкой субтитров.
     *
     * @param videoId  идентификатор видео
     * @param language язык дорожки
     * @param xml      XML субтитров
     */
    public void videoWithCaptions(String videoId, String language, String xml) {
        stubWatchPage(videoId);
        var trackUrl = server.baseUrl() + "/api/timedtext?v=" + videoId + "&lang=" + language + "&fmt=srv3";
        stubPlayer(videoId, """
                {"playabilityStatus":{"status":"OK"},
                 "captions":{"playerCaptionsTracklistRenderer":{"captionTracks":[
                   {"baseUrl":"%s","languageCode":"%s"}]}}}""".formatted(trackUrl, language));
        server.stubFor(get(urlPathEqualTo("/api/timedtext")).withQueryParam("v", equalTo(videoId))
                .willReturn(aResponse().withHeader("Content-Type", "text/xml").withBody(xml)));
    }

    /**
     * Видео без субтитров.
     *
     * @param videoId идентификатор видео
     */
    public void videoWithoutCaptions(String videoId) {
        stubWatchPage(videoId);
        stubPlayer(videoId, "{\"playabilityStatus\":{\"status\":\"OK\"}}");
    }

    /**
     * Страница видео с ключом innertube API.
     *
     * @param videoId идентификатор видео
     */
    private void stubWatchPage(String videoId) {
        server.stubFor(get(urlPathEqualTo("/watch")).withQueryParam("v", equalTo(videoId))
                .willReturn(aResponse().withBody("<script>ytcfg.set({\"INNERTUBE_API_KEY\":\"test-key\"})</script>")));
    }

    /**
     * Ответ плеера для видео.
     *
     * @param videoId идентификатор видео
     * @param json    тело ответа
     */
    private void stubPlayer(String videoId, String json) {
        server.stubFor(post(urlPathEqualTo("/youtubei/v1/player"))
                .withQueryParam("key", equalTo("test-key"))
                .withRequestBody(matchingJsonPath("$.videoId", equalTo(videoId)))
                .willReturn(okJson(json)));
    }
}
