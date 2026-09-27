package net.pamytno.topic.integration.youtube;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты {@link TranscriptXmlParser}.
 */
class TranscriptXmlParserTest {

    @Test
    @DisplayName("Каждая реплика становится строкой, HTML-сущности раскодируются, пустые реплики пропускаются")
    void parse_extractsLines() {
        var xml = """
                <?xml version="1.0" encoding="utf-8" ?><transcript>
                <text start="0.0" dur="1.5">JVM выполняет</text>
                <text start="1.5" dur="1.0">  </text>
                <text start="2.5" dur="2.0">байткод &amp;#39;Java&amp;#39;</text>
                </transcript>""";

        assertThat(TranscriptXmlParser.parse(xml)).isEqualTo("JVM выполняет\nбайткод 'Java'");
    }

    @Test
    @DisplayName("Битый XML даёт YoutubeTranscriptException")
    void parse_throws_forBrokenXml() {
        assertThatThrownBy(() -> TranscriptXmlParser.parse("<transcript><text>")).isInstanceOf(
                YoutubeTranscriptException.class);
    }

    @Test
    @DisplayName("DOCTYPE запрещён — защита от XXE")
    void parse_rejectsDoctype() {
        var xml = "<!DOCTYPE t [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><transcript><text>&x;</text></transcript>";

        assertThatThrownBy(() -> TranscriptXmlParser.parse(xml)).isInstanceOf(YoutubeTranscriptException.class);
    }
}
