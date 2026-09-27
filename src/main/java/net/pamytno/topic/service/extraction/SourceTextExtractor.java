package net.pamytno.topic.service.extraction;

import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;

/**
 * Стратегия получения текста из источника определённого вида.
 */
public interface SourceTextExtractor {

    /**
     * Проверяет, умеет ли стратегия обрабатывать вид источника.
     *
     * @param type вид источника
     * @return {@code true}, если умеет
     */
    boolean supports(SourceType type);

    /**
     * Извлекает «сырой» текст (до технической очистки).
     *
     * @param source источник
     * @return извлечённый текст
     * @throws TextExtractionException если текст получить не удалось
     */
    String extract(Source source);
}
