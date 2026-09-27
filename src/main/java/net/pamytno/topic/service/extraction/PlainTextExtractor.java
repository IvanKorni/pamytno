package net.pamytno.topic.service.extraction;

import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;
import org.springframework.stereotype.Component;

/**
 * Текст и список слов уже являются текстом — отдаём исходный материал как есть.
 */
@Component
public class PlainTextExtractor implements SourceTextExtractor {

    /**
     * Поддерживает {@link SourceType#TEXT} и {@link SourceType#WORD_LIST}.
     *
     * @param type вид источника
     * @return {@code true} для текстовых видов
     */
    @Override
    public boolean supports(SourceType type) {
        return type.isTextual();
    }

    /**
     * Возвращает исходный текст источника.
     *
     * @param source источник
     * @return исходный текст
     */
    @Override
    public String extract(Source source) {
        return source.getOriginalText();
    }
}
