package net.pamytno.topic;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Генерирует PDF-файлы для тестов прямо в памяти.
 */
public final class PdfFixtures {

    private static final float FONT_SIZE = 12;
    private static final float LEFT = 50;
    private static final float TOP = 700;
    private static final float LINE_HEIGHT = 15;

    /**
     * Запрещает создание экземпляров.
     */
    private PdfFixtures() {
    }

    /**
     * PDF с текстовым слоем (латиница — стандартный шрифт PDF не содержит кириллицы).
     *
     * @param lines строки текста
     * @return содержимое PDF
     */
    public static byte[] withText(String... lines) {
        try (var document = new PDDocument()) {
            var page = new PDPage();
            document.addPage(page);
            try (var stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                stream.newLineAtOffset(LEFT, TOP);
                for (var line : lines) {
                    stream.showText(line);
                    stream.newLineAtOffset(0, -LINE_HEIGHT);
                }
                stream.endText();
            }
            return save(document);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * PDF без текстового слоя — как скан из картинок.
     *
     * @return содержимое PDF
     */
    public static byte[] withoutText() {
        try (var document = new PDDocument()) {
            document.addPage(new PDPage());
            return save(document);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Сохраняет документ в массив байтов.
     *
     * @param document документ
     * @return содержимое PDF
     * @throws IOException при ошибке записи
     */
    private static byte[] save(PDDocument document) throws IOException {
        var out = new ByteArrayOutputStream();
        document.save(out);
        return out.toByteArray();
    }
}
