package net.pamytno.topic.integration.youtube;

import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Разбирает XML субтитров YouTube ({@code <transcript><text start dur>…</text></transcript>}) в текст:
 * одна реплика — одна строка. Внешние сущности XML запрещены.
 */
public final class TranscriptXmlParser {

    /**
     * Запрещает создание экземпляров.
     */
    private TranscriptXmlParser() {
    }

    /**
     * Извлекает текст реплик.
     *
     * @param xml XML субтитров
     * @return реплики через перевод строки
     * @throws YoutubeTranscriptException если XML не разбирается
     */
    public static String parse(String xml) {
        var texts = parseDocument(xml).getElementsByTagName("text");
        var lines = new ArrayList<String>(texts.getLength());
        for (var index = 0; index < texts.getLength(); index++) {
            lines.add(HtmlUtils.htmlUnescape(texts.item(index).getTextContent()).strip());
        }
        return String.join("\n", nonBlank(lines));
    }

    /**
     * Безопасно разбирает XML.
     *
     * @param xml XML субтитров
     * @return DOM-документ
     */
    private static Document parseDocument(String xml) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new YoutubeTranscriptException("Не удалось разобрать субтитры", e);
        }
    }

    /**
     * Убирает пустые реплики.
     *
     * @param lines реплики
     * @return непустые реплики
     */
    private static List<String> nonBlank(List<String> lines) {
        return lines.stream().filter(line -> !line.isBlank()).toList();
    }
}
