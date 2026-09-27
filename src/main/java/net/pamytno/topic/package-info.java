/**
 * Модуль «Темы и материалы»: темы, источники (PDF, YouTube, текст, список слов),
 * извлечение текста и сборка единого текста темы (Master Text).
 * Публикует {@code TopicContentPrepared} и {@code TopicDeleted}.
 */
@ApplicationModule(displayName = "Темы и материалы", allowedDependencies = "common")
package net.pamytno.topic;

import org.springframework.modulith.ApplicationModule;
