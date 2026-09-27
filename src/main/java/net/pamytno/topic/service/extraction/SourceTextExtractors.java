package net.pamytno.topic.service.extraction;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.domain.TextCleaner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Выбирает стратегию по виду источника и возвращает очищенный текст.
 */
@Component
@RequiredArgsConstructor
public class SourceTextExtractors {

    private final List<SourceTextExtractor> extractors;

    /**
     * Извлекает текст и выполняет техническую очистку.
     *
     * @param source источник
     * @return непустой очищенный текст
     * @throws TextExtractionException если стратегии нет или текст пуст
     */
    public String extract(Source source) {
        var extractor = extractors.stream()
                .filter(candidate -> candidate.supports(source.getType()))
                .findFirst()
                .orElseThrow(() -> new TextExtractionException(SourceErrorCode.SOURCE_PROCESSING_FAILED,
                        "Источники вида " + source.getType() + " не поддерживаются"));
        var text = TextCleaner.clean(extractor.extract(source));
        if (text.isEmpty()) {
            throw new TextExtractionException(SourceErrorCode.TEXT_EXTRACTION_FAILED,
                    "Не удалось извлечь текст из источника");
        }
        return text;
    }
}
