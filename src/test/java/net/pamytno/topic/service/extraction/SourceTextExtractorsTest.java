package net.pamytno.topic.service.extraction;

import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.Topic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты {@link SourceTextExtractors}.
 */
class SourceTextExtractorsTest {

    private static final Topic TOPIC = Topic.create(UUID.randomUUID(), "JVM", null, Instant.EPOCH);

    private final SourceTextExtractors extractors = new SourceTextExtractors(List.of(new PlainTextExtractor()));

    @Test
    @DisplayName("Текст извлекается подходящей стратегией и очищается")
    void extract_returnsCleanedText() {
        var source = Source.text(TOPIC, SourceType.TEXT, null, "  JVM   выполняет\n\n байткод ", Instant.EPOCH);

        assertThat(extractors.extract(source)).isEqualTo("JVM выполняет\nбайткод");
    }

    @Test
    @DisplayName("Пустой после очистки текст — ошибка TEXT_EXTRACTION_FAILED")
    void extract_throws_whenTextEmptyAfterCleaning() {
        var source = Source.text(TOPIC, SourceType.TEXT, null, " \n ", Instant.EPOCH);

        assertThatThrownBy(() -> extractors.extract(source))
                .isInstanceOf(TextExtractionException.class)
                .extracting("errorCode").isEqualTo(SourceErrorCode.TEXT_EXTRACTION_FAILED);
    }

    @Test
    @DisplayName("Вид без стратегии — ошибка SOURCE_PROCESSING_FAILED")
    void extract_throws_whenNoExtractorSupportsType() {
        var noExtractors = new SourceTextExtractors(List.of());
        var source = Source.text(TOPIC, SourceType.TEXT, null, "текст", Instant.EPOCH);

        assertThatThrownBy(() -> noExtractors.extract(source))
                .isInstanceOf(TextExtractionException.class)
                .extracting("errorCode").isEqualTo(SourceErrorCode.SOURCE_PROCESSING_FAILED);
    }
}
