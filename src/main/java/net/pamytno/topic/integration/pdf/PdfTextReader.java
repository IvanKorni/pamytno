package net.pamytno.topic.integration.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Извлекает текстовый слой PDF через Apache PDFBox. OCR не выполняется:
 * у PDF из картинок текст будет пустым.
 */
@Component
public class PdfTextReader {

    /**
     * Читает текст всех страниц.
     *
     * @param file PDF-файл
     * @return текст документа, возможно пустой
     * @throws IOException если файл повреждён, зашифрован или не читается
     */
    public String read(Path file) throws IOException {
        try (var document = Loader.loadPDF(file.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }
}
