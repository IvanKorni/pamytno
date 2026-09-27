package net.pamytno.topic.integration.youtube;

import net.pamytno.support.TestWireMock;
import net.pamytno.topic.YoutubeStubs;
import net.pamytno.topic.config.YoutubeClientConfig;
import net.pamytno.topic.config.YoutubeProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тест {@link YoutubeTranscriptClient} против WireMock без Spring-контекста.
 */
class YoutubeTranscriptClientTest {

    private final YoutubeStubs stubs = new YoutubeStubs(TestWireMock.started());
    private final YoutubeProperties properties = new YoutubeProperties(TestWireMock.started().baseUrl(),
            List.of("ru", "en"), Duration.ofSeconds(5), "ANDROID", "20.10.38");
    private final YoutubeTranscriptClient client = new YoutubeTranscriptClient(
            new YoutubeClientConfig().youtubeRestClient(properties), properties);

    @Test
    @DisplayName("Субтитры видео загружаются по цепочке страница → плеер → XML")
    void fetchTranscript_returnsCaptionText() {
        var videoId = YoutubeStubs.randomVideoId();
        stubs.videoWithCaptions(videoId, "ru", "<transcript><text>Привет</text><text>мир</text></transcript>");

        assertThat(client.fetchTranscript(videoId)).isEqualTo("Привет\nмир");
    }

    @Test
    @DisplayName("Видео без субтитров даёт YoutubeTranscriptException")
    void fetchTranscript_throws_whenNoCaptions() {
        var videoId = YoutubeStubs.randomVideoId();
        stubs.videoWithoutCaptions(videoId);

        assertThatThrownBy(() -> client.fetchTranscript(videoId)).isInstanceOf(YoutubeTranscriptException.class);
    }

    @Test
    @DisplayName("Ошибка HTTP YouTube превращается в YoutubeTranscriptException")
    void fetchTranscript_throws_whenYoutubeFails() {
        assertThatThrownBy(() -> client.fetchTranscript(YoutubeStubs.randomVideoId()))
                .isInstanceOf(YoutubeTranscriptException.class);
    }
}
