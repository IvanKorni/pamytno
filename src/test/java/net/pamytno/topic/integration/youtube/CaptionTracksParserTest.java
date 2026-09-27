package net.pamytno.topic.integration.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты {@link CaptionTracksParser}.
 */
class CaptionTracksParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Дорожки читаются из ответа плеера, kind=asr означает автоматические субтитры")
    void parse_readsTracks() throws Exception {
        var player = objectMapper.readTree("""
                {"playabilityStatus":{"status":"OK"},
                 "captions":{"playerCaptionsTracklistRenderer":{"captionTracks":[
                   {"baseUrl":"u1","languageCode":"ru"},
                   {"baseUrl":"u2","languageCode":"en","kind":"asr"}]}}}""");

        assertThat(CaptionTracksParser.parse(player)).containsExactly(
                new CaptionTrack("ru", "u1", false), new CaptionTrack("en", "u2", true));
    }

    @Test
    @DisplayName("Недоступное видео и видео без субтитров дают YoutubeTranscriptException")
    void parse_throws_whenUnavailableOrNoCaptions() throws Exception {
        var unavailable = objectMapper.readTree(
                "{\"playabilityStatus\":{\"status\":\"ERROR\",\"reason\":\"Удалено\"}}");
        var noCaptions = objectMapper.readTree("{\"playabilityStatus\":{\"status\":\"OK\"}}");

        assertThatThrownBy(() -> CaptionTracksParser.parse(unavailable))
                .isInstanceOf(YoutubeTranscriptException.class).hasMessageContaining("Удалено");
        assertThatThrownBy(() -> CaptionTracksParser.parse(noCaptions))
                .isInstanceOf(YoutubeTranscriptException.class);
    }
}
