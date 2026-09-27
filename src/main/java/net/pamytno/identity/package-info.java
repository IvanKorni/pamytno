/**
 * Модуль «Пользователи»: регистрация, вход по email и паролю, выпуск JWT.
 * Другие модули узнают пользователя только по {@code userId} из токена.
 */
@ApplicationModule(displayName = "Пользователи", allowedDependencies = "common")
package net.pamytno.identity;

import org.springframework.modulith.ApplicationModule;
