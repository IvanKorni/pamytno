package net.pamytno.topic.domain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Собирает единый текст темы: извлечённые тексты готовых источников в порядке добавления,
 * разделённые пустой строкой. Текст не сокращается и не переписывается.
 */
public final class MasterTextBuilder {

    /** Разделитель текстов разных источников. */
    public static final String SOURCE_SEPARATOR = "\n\n";

    /**
     * Запрещает создание экземпляров.
     */
    private MasterTextBuilder() {
    }

    /**
     * Строит единый текст.
     *
     * @param sources источники темы в порядке добавления
     * @return объединённый текст готовых источников; пустая строка, если готовых нет
     */
    public static String build(List<Source> sources) {
        return sources.stream()
                .filter(Source::isReady)
                .map(Source::getExtractedText)
                .collect(Collectors.joining(SOURCE_SEPARATOR));
    }
}
