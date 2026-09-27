package net.pamytno.common.event.topic;

import java.util.UUID;

/**
 * Собрана новая версия единого текста темы. Модуль карточек строит по нему фрагменты
 * для генерации вопросов. Пустой {@code content} означает, что готовых материалов не осталось.
 *
 * @param topicId идентификатор темы
 * @param userId  владелец темы
 * @param version номер версии текста, начиная с 1
 * @param content единый текст темы
 */
public record TopicContentPrepared(UUID topicId, UUID userId, int version, String content) {
}
