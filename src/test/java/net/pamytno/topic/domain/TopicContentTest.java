package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link TopicContent}.
 */
class TopicContentTest {

    private static final UUID TOPIC_ID = UUID.randomUUID();

    @Test
    @DisplayName("Первая версия имеет номер 1, следующая — на единицу больше и новый идентификатор")
    void next_incrementsVersion() {
        var first = TopicContent.first(TOPIC_ID, "текст", Instant.EPOCH);

        var second = first.next("новый текст", Instant.EPOCH);

        assertThat(first.getVersion()).isEqualTo(1);
        assertThat(second.getVersion()).isEqualTo(2);
        assertThat(second.getTopicId()).isEqualTo(TOPIC_ID);
        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(second.getContent()).isEqualTo("новый текст");
    }

    @Test
    @DisplayName("Сравнение текста версии")
    void hasContent_comparesText() {
        var content = TopicContent.first(TOPIC_ID, "текст", Instant.EPOCH);

        assertThat(content.hasContent("текст")).isTrue();
        assertThat(content.hasContent("другой")).isFalse();
    }
}
