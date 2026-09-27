package net.pamytno.deck.domain;

/**
 * Фрагмент текста с положением в исходном едином тексте темы.
 *
 * @param sequenceNumber порядковый номер фрагмента, начиная с 1
 * @param startOffset    смещение начала в едином тексте (включительно)
 * @param endOffset      смещение конца в едином тексте (не включительно)
 * @param content        текст фрагмента, равный {@code text.substring(startOffset, endOffset)}
 */
public record ChunkSpan(int sequenceNumber, int startOffset, int endOffset, String content) {
}
