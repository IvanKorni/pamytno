package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link MasterTextBuilder}.
 */
class MasterTextBuilderTest {

    private static final Topic TOPIC = Topic.create(UUID.randomUUID(), "JVM", null, Instant.EPOCH);

    @Test
    @DisplayName("Тексты готовых источников объединяются в порядке добавления через пустую строку")
    void build_joinsReadySourcesInOrder() {
        var first = readySource("Первый источник");
        var second = readySource("Второй источник");

        assertThat(MasterTextBuilder.build(List.of(first, second)))
                .isEqualTo("Первый источник\n\nВторой источник");
    }

    @Test
    @DisplayName("Источники в обработке и с ошибкой в текст не попадают")
    void build_skipsNotReadySources() {
        var failed = Source.text(TOPIC, SourceType.TEXT, null, "x", Instant.EPOCH);
        failed.failExtraction(SourceErrorCode.TEXT_EXTRACTION_FAILED, "ошибка", Instant.EPOCH);
        var uploaded = Source.text(TOPIC, SourceType.TEXT, null, "y", Instant.EPOCH);

        assertThat(MasterTextBuilder.build(List.of(failed, readySource("Готовый"), uploaded))).isEqualTo("Готовый");
    }

    @Test
    @DisplayName("Без готовых источников текст пустой")
    void build_returnsEmpty_whenNoReadySources() {
        assertThat(MasterTextBuilder.build(List.of())).isEmpty();
    }

    /**
     * Создаёт обработанный источник.
     *
     * @param text извлечённый текст
     * @return источник в статусе READY
     */
    private static Source readySource(String text) {
        var source = Source.text(TOPIC, SourceType.TEXT, null, text, Instant.EPOCH);
        source.completeExtraction(text, Instant.EPOCH);
        return source;
    }
}
