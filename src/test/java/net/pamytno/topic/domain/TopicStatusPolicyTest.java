package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.pamytno.topic.domain.SourceStatus.ERROR;
import static net.pamytno.topic.domain.SourceStatus.PROCESSING;
import static net.pamytno.topic.domain.SourceStatus.READY;
import static net.pamytno.topic.domain.SourceStatus.UPLOADED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link TopicStatusPolicy}.
 */
class TopicStatusPolicyTest {

    @Test
    @DisplayName("Без источников тема в DRAFT")
    void resolve_draft_whenNoSources() {
        assertThat(TopicStatusPolicy.resolve(List.of())).isEqualTo(TopicStatus.DRAFT);
    }

    @Test
    @DisplayName("Хотя бы один источник в обработке — тема в PROCESSING")
    void resolve_processing_whenAnySourceInProgress() {
        assertThat(TopicStatusPolicy.resolve(List.of(READY, UPLOADED))).isEqualTo(TopicStatus.PROCESSING);
        assertThat(TopicStatusPolicy.resolve(List.of(ERROR, PROCESSING))).isEqualTo(TopicStatus.PROCESSING);
    }

    @Test
    @DisplayName("Есть готовый источник — тема READY, даже если другие с ошибкой")
    void resolve_ready_whenAnySourceReady() {
        assertThat(TopicStatusPolicy.resolve(List.of(ERROR, READY))).isEqualTo(TopicStatus.READY);
    }

    @Test
    @DisplayName("Все источники с ошибкой — тема в ERROR")
    void resolve_error_whenAllSourcesFailed() {
        assertThat(TopicStatusPolicy.resolve(List.of(ERROR, ERROR))).isEqualTo(TopicStatus.ERROR);
    }
}
