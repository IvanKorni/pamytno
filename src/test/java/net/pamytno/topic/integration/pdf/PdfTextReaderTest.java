package net.pamytno.topic.integration.pdf;

import net.pamytno.topic.PdfFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты {@link PdfTextReader} на настоящих PDF, сгенерированных PDFBox.
 */
class PdfTextReaderTest {

    private final PdfTextReader reader = new PdfTextReader();

    @TempDir
    private Path directory;

    @Test
    @DisplayName("Текстовый слой PDF извлекается построчно")
    void read_extractsTextLayer() throws IOException {
        var file = Files.write(directory.resolve("notes.pdf"), PdfFixtures.withText("JVM executes", "bytecode"));

        assertThat(reader.read(file)).contains("JVM executes").contains("bytecode");
    }

    @Test
    @DisplayName("У PDF без текстового слоя текст пустой")
    void read_returnsBlank_forImageOnlyPdf() throws IOException {
        var file = Files.write(directory.resolve("scan.pdf"), PdfFixtures.withoutText());

        assertThat(reader.read(file)).isBlank();
    }

    @Test
    @DisplayName("Повреждённый файл даёт IOException")
    void read_throws_forBrokenFile() throws IOException {
        var file = Files.writeString(directory.resolve("broken.pdf"), "%PDF-1.7 это не PDF");

        assertThatThrownBy(() -> reader.read(file)).isInstanceOf(IOException.class);
    }
}
