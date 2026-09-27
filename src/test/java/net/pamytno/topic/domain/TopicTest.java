package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты сущности {@link Topic}.
 */
class TopicTest {

    private static final Instant CREATED = Instant.parse("2026-09-27T10:00:00Z");
    private static final Instant LATER = CREATED.plusSeconds(60);
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Новая тема создаётся в статусе DRAFT с владельцем и датами")
    void create_startsAsDraft() {
        var topic = Topic.create(USER_ID, "Spring Security", "Фильтры", CREATED);

        assertThat(topic.getId()).isNotNull();
        assertThat(topic.getUserId()).isEqualTo(USER_ID);
        assertThat(topic.getStatus()).isEqualTo(TopicStatus.DRAFT);
        assertThat(topic.getDescription()).isEqualTo("Фильтры");
        assertThat(topic.getCreatedAt()).isEqualTo(CREATED);
    }

    @Test
    @DisplayName("Пустое описание не сохраняется")
    void describe_clearsBlankDescription() {
        var topic = Topic.create(USER_ID, "JVM", "Описание", CREATED);

        topic.describe("   ", LATER);

        assertThat(topic.getDescription()).isNull();
        assertThat(topic.getUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    @DisplayName("Переименование меняет название и дату изменения")
    void rename_changesTitle() {
        var topic = Topic.create(USER_ID, "JVM", null, CREATED);

        topic.rename("Java Virtual Machine", LATER);

        assertThat(topic.getTitle()).isEqualTo("Java Virtual Machine");
        assertThat(topic.getUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    @DisplayName("Смена статуса на тот же не трогает дату изменения")
    void changeStatus_ignoresSameStatus() {
        var topic = Topic.create(USER_ID, "JVM", null, CREATED);

        topic.changeStatus(TopicStatus.DRAFT, LATER);
        assertThat(topic.getUpdatedAt()).isEqualTo(CREATED);

        topic.changeStatus(TopicStatus.PROCESSING, LATER);
        assertThat(topic.getStatus()).isEqualTo(TopicStatus.PROCESSING);
        assertThat(topic.getUpdatedAt()).isEqualTo(LATER);
    }
}
