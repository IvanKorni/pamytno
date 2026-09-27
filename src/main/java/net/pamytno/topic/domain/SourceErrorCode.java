package net.pamytno.topic.domain;

/**
 * Причина, по которой источник перешёл в {@link SourceStatus#ERROR}.
 */
public enum SourceErrorCode {

    /** Из файла или текста не удалось получить текст (например, PDF из картинок). */
    TEXT_EXTRACTION_FAILED,
    /** У видео нет доступных субтитров или YouTube их не отдал. */
    TRANSCRIPT_UNAVAILABLE,
    /** Непредвиденная ошибка обработки. */
    SOURCE_PROCESSING_FAILED
}
