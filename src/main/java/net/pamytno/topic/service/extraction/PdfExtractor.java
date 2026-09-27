package net.pamytno.topic.service.extraction;

import lombok.RequiredArgsConstructor;
import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceErrorCode;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.domain.TextCleaner;
import net.pamytno.topic.integration.pdf.PdfTextReader;
import net.pamytno.topic.integration.storage.LocalFileStorage;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Извлекает текст PDF из хранилища и склеивает слова, разорванные переносами строк.
 */
@Component
@RequiredArgsConstructor
public class PdfExtractor implements SourceTextExtractor {

    private final LocalFileStorage storage;
    private final PdfTextReader pdfTextReader;

    /**
     * Поддерживает {@link SourceType#PDF}.
     *
     * @param type вид источника
     * @return {@code true} для PDF
     */
    @Override
    public boolean supports(SourceType type) {
        return type == SourceType.PDF;
    }

    /**
     * Читает текстовый слой PDF.
     *
     * @param source источник PDF
     * @return текст документа
     * @throws TextExtractionException если PDF повреждён, зашифрован или не читается
     */
    @Override
    public String extract(Source source) {
        try {
            return TextCleaner.joinHyphenatedLineBreaks(pdfTextReader.read(storage.resolve(source.getStorageKey())));
        } catch (IOException e) {
            throw new TextExtractionException(SourceErrorCode.TEXT_EXTRACTION_FAILED,
                    "Не удалось прочитать PDF: файл повреждён или защищён паролем", e);
        }
    }
}
