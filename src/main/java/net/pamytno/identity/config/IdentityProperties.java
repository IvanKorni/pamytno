package net.pamytno.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Настройки модуля пользователей ({@code pamytno.identity}).
 *
 * @param registrationEnabled разрешена ли открытая регистрация; для личного развёртывания
 *                            её выключают после создания своего пользователя
 */
@ConfigurationProperties("pamytno.identity")
public record IdentityProperties(@DefaultValue("true") boolean registrationEnabled) {
}
