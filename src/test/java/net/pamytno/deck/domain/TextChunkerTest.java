package net.pamytno.deck.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link TextChunker}.
 */
class TextChunkerTest {

    @Test
    @DisplayName("Короткий текст — один фрагмент без крайних пробелов с точными смещениями")
    void split_returnsSingleChunk_forShortText() {
        var text = "  JVM выполняет байткод.  ";

        var spans = TextChunker.split(text, 100);

        assertThat(spans).containsExactly(new ChunkSpan(1, 2, 24, "JVM выполняет байткод."));
    }

    @Test
    @DisplayName("Длинный текст режется по границе абзаца")
    void split_prefersParagraphBoundary() {
        var text = "Первый абзац про JVM.\n\nВторой абзац про GC.";

        var spans = TextChunker.split(text, 30);

        assertThat(spans).extracting(ChunkSpan::content)
                .containsExactly("Первый абзац про JVM.", "Второй абзац про GC.");
    }

    @Test
    @DisplayName("Без абзацев и строк текст режется по концу предложения")
    void split_fallsBackToSentenceBoundary() {
        var text = "Первое предложение тут. Второе предложение там.";

        var spans = TextChunker.split(text, 30);

        assertThat(spans).extracting(ChunkSpan::content)
                .containsExactly("Первое предложение тут.", "Второе предложение там.");
    }

    @Test
    @DisplayName("Текст без границ режется жёстко по максимальной длине")
    void split_hardCuts_whenNoBoundary() {
        var spans = TextChunker.split("a".repeat(25), 10);

        assertThat(spans).extracting(ChunkSpan::content).containsExactly("a".repeat(10), "a".repeat(10), "aaaaa");
    }

    @Test
    @DisplayName("Смещения каждого фрагмента указывают ровно на его текст в исходнике, номера идут с 1")
    void split_offsetsPointToOriginalText() {
        var text = "Строка один\nстрока два\n\nАбзац три. Предложение четыре! Вопрос пять? Хвост.";

        var spans = TextChunker.split(text, 20);

        assertThat(spans).allSatisfy(span ->
                assertThat(text.substring(span.startOffset(), span.endOffset())).isEqualTo(span.content()));
        assertThat(spans).extracting(ChunkSpan::sequenceNumber)
                .containsExactlyElementsOf(IntStream.rangeClosed(1, spans.size()).boxed().toList());
        assertThat(spans).allSatisfy(span -> assertThat(span.content()).hasSizeLessThanOrEqualTo(20));
    }

    @Test
    @DisplayName("Пустой и пробельный текст даёт пустой список")
    void split_returnsEmpty_forBlankText() {
        assertThat(TextChunker.split("", 10)).isEmpty();
        assertThat(TextChunker.split(" \n\t ", 10)).isEmpty();
    }
}
