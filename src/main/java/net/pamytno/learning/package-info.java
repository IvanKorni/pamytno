/**
 * Модуль «Обучение»: интервальное повторение карточек, учебные сессии и прогресс.
 * О карточках узнаёт из событий модуля {@code deck}, об удалении тем — из {@code topic}.
 */
@ApplicationModule(displayName = "Обучение", allowedDependencies = "common")
package net.pamytno.learning;

import org.springframework.modulith.ApplicationModule;
