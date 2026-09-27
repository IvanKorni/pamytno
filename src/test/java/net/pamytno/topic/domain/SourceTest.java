package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты сущности {@link Source}.
 */
class SourceTest {

    private static final Instant NOW = Instant.parse("2026-09-27T10:00:00Z");
    private static final Topic TOPIC = Topic.create(UUID.randomUUID(), "JVM", null, NOW);

    @Test
    @DisplayName("Текстовый источник хранит исходный текст как есть и привязан к теме и владельцу")
    void text_keepsOriginalTextUnchanged() {
        var source = Source.text(TOPIC, SourceType.TEXT, "Конспект", "  Сырой   текст  ", NOW);

        assertThat(source.getOriginalText()).isEqualTo("  Сырой   текст  ");
        assertThat(source.getTopicId()).isEqualTo(TOPIC.getId());
        assertThat(source.getUserId()).isEqualTo(TOPIC.getUserId());
        assertThat(source.getStatus()).isEqualTo(SourceStatus.UPLOADED);
    }

    @Test
    @DisplayName("Нетекстовый вид нельзя создать как текстовый источник")
    void text_rejectsNonTextualType() {
        assertThatThrownBy(() -> Source.text(TOPIC, SourceType.PDF, null, "текст", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Успешная обработка сохраняет извлечённый текст отдельно от исходного")
    void completeExtraction_storesExtractedTextSeparately() {
        var source = Source.text(TOPIC, SourceType.WORD_LIST, null, " cat — кот ", NOW);

        source.startProcessing(NOW);
        source.completeExtraction("cat — кот", NOW);

        assertThat(source.getStatus()).isEqualTo(SourceStatus.READY);
        assertThat(source.isReady()).isTrue();
        assertThat(source.getExtractedText()).isEqualTo("cat — кот");
        assertThat(source.getOriginalText()).isEqualTo(" cat — кот ");
    }

    @Test
    @DisplayName("Ошибка обработки сохраняет код и обрезает слишком длинное сообщение")
    void failExtraction_storesErrorAndTruncatesMessage() {
        var source = Source.text(TOPIC, SourceType.TEXT, null, "текст", NOW);

        source.failExtraction(SourceErrorCode.TEXT_EXTRACTION_FAILED, "х".repeat(1500), NOW);

        assertThat(source.getStatus()).isEqualTo(SourceStatus.ERROR);
        assertThat(source.getErrorCode()).isEqualTo(SourceErrorCode.TEXT_EXTRACTION_FAILED);
        assertThat(source.getErrorMessage()).hasSize(1000);
    }

    @Test
    @DisplayName("PDF-источник получает ключ хранилища вида <тема>/<источник>.pdf")
    void pdf_buildsStorageKeyFromTopicAndSource() {
        var source = Source.pdf(TOPIC, "notes.pdf", NOW);

        assertThat(source.getType()).isEqualTo(SourceType.PDF);
        assertThat(source.getOriginalName()).isEqualTo("notes.pdf");
        assertThat(source.getStorageKey()).isEqualTo(TOPIC.getId() + "/" + source.getId() + ".pdf");
    }
}
