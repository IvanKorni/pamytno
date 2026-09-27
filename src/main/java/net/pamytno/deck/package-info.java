/**
 * Модуль «Вопросы и карточки»: фрагменты единого текста, генерация вопросов через AI,
 * отбор вопросов пользователем, генерация и редактирование карточек.
 * Слушает {@code TopicContentPrepared} и {@code TopicDeleted}, публикует события о карточках.
 */
@ApplicationModule(displayName = "Вопросы и карточки", allowedDependencies = "common")
package net.pamytno.deck;

import org.springframework.modulith.ApplicationModule;
