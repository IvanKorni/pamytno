package net.pamytno.deck.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Делит единый текст темы на фрагменты, помещающиеся в контекст модели.
 *
 * <p>Граница ищется во второй половине допустимой длины: сначала абзац, затем строка, затем
 * конец предложения; если ничего нет — жёсткий разрез. Текст фрагментов не меняется,
 * только обрезаются пробелы по краям, поэтому смещения точно указывают на исходный текст.
 */
public final class TextChunker {

    private static final List<String> BREAKS = List.of("\n\n", "\n", ". ", "! ", "? ");

    /**
     * Запрещает создание экземпляров.
     */
    private TextChunker() {
    }

    /**
     * Делит текст на фрагменты.
     *
     * @param text      единый текст темы
     * @param maxLength максимальная длина фрагмента в символах
     * @return фрагменты по порядку; пустой список для пустого текста
     */
    public static List<ChunkSpan> split(String text, int maxLength) {
        var spans = new ArrayList<ChunkSpan>();
        var start = skipWhitespace(text, 0);
        while (start < text.length()) {
            var end = chunkEnd(text, start, maxLength);
            var contentEnd = trimEnd(text, start, end);
            spans.add(new ChunkSpan(spans.size() + 1, start, contentEnd, text.substring(start, contentEnd)));
            start = skipWhitespace(text, end);
        }
        return spans;
    }

    /**
     * Находит конец фрагмента, начинающегося в {@code start}.
     *
     * @param text      текст
     * @param start     начало фрагмента
     * @param maxLength максимальная длина
     * @return позиция сразу после фрагмента
     */
    private static int chunkEnd(String text, int start, int maxLength) {
        var limit = start + maxLength;
        if (limit >= text.length()) {
            return text.length();
        }
        var earliest = start + maxLength / 2;
        return BREAKS.stream()
                .map(separator -> lastBreak(text, earliest, limit, separator))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(() -> Character.isHighSurrogate(text.charAt(limit - 1)) ? limit - 1 : limit);
    }

    /**
     * Последний разделитель, целиком лежащий в {@code [from, to)}.
     *
     * @param text      текст
     * @param from      самая ранняя допустимая позиция разделителя
     * @param to        граница поиска
     * @param separator разделитель
     * @return позиция сразу после разделителя
     */
    private static Optional<Integer> lastBreak(String text, int from, int to, String separator) {
        var index = text.lastIndexOf(separator, to - separator.length());
        return index >= from ? Optional.of(index + separator.length()) : Optional.empty();
    }

    /**
     * Пропускает пробельные символы.
     *
     * @param text  текст
     * @param index начальная позиция
     * @return первая непробельная позиция или длина текста
     */
    private static int skipWhitespace(String text, int index) {
        var position = index;
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
        return position;
    }

    /**
     * Отбрасывает пробельные символы в конце фрагмента.
     *
     * @param text  текст
     * @param start начало фрагмента
     * @param end   конец фрагмента
     * @return конец без хвостовых пробелов
     */
    private static int trimEnd(String text, int start, int end) {
        var position = end;
        while (position > start && Character.isWhitespace(text.charAt(position - 1))) {
            position--;
        }
        return position;
    }
}
